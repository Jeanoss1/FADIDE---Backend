package pe.com.fadide.sisco.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.com.fadide.sisco.common.exception.RecursoNoEncontradoException;
import pe.com.fadide.sisco.common.service.impl.CrudServiceImpl;
import pe.com.fadide.sisco.config.BadRequestException;
import pe.com.fadide.sisco.config.ConflictException;
import pe.com.fadide.sisco.dto.LicenciaRequest;
import pe.com.fadide.sisco.dto.LicenciaResponse;
import pe.com.fadide.sisco.model.Licencia;
import pe.com.fadide.sisco.model.Proyecto;
import pe.com.fadide.sisco.repository.LicenciaRepository;
import pe.com.fadide.sisco.repository.ProyectoRepository;
import pe.com.fadide.sisco.service.LicenciaService;

// Patrón Singleton: bean de Spring con una única instancia
@Service
@RequiredArgsConstructor
public class LicenciaServiceImpl extends CrudServiceImpl<Licencia, LicenciaRequest, LicenciaResponse, Long>
        implements LicenciaService {

    private final LicenciaRepository licenciaRepository;
    private final ProyectoRepository proyectoRepository;

    @Override
    @Transactional(readOnly = true)
    public LicenciaResponse obtenerPorProyecto(Long idProyecto) {
        return licenciaRepository.buscarPorProyecto(idProyecto)
                .map(this::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una licencia para el proyecto con id " + idProyecto));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LicenciaResponse> listarPorVencer(int dias) {
        if (dias < 0) {
            throw new BadRequestException("El número de días no puede ser negativo");
        }
        LocalDate hoy = LocalDate.now();
        return licenciaRepository.buscarPorVencer(hoy, hoy.plusDays(dias)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    protected JpaRepository<Licencia, Long> getRepository() {
        return licenciaRepository;
    }

    @Override
    protected String mensajeNoEncontrado(Long id) {
        return "Licencia no encontrada con id " + id;
    }

    @Override
    protected void validarCrear(LicenciaRequest request) {
        validarFechas(request);
        if (licenciaRepository.existsByNumero(request.numero())) {
            throw new ConflictException("Ya existe una licencia con el número " + request.numero());
        }
        if (licenciaRepository.existsByProyectoId(request.idProyecto())) {
            throw new ConflictException("El proyecto ya tiene una licencia asociada");
        }
    }

    @Override
    protected void validarActualizar(Long id, LicenciaRequest request) {
        validarFechas(request);
        if (licenciaRepository.existsByNumeroAndIdLicenciaNot(request.numero(), id)) {
            throw new ConflictException("Ya existe una licencia con el número " + request.numero());
        }
        if (licenciaRepository.existsByProyectoIdAndIdLicenciaNot(request.idProyecto(), id)) {
            throw new ConflictException("El proyecto ya tiene una licencia asociada");
        }
    }

    @Override
    protected Licencia toEntity(LicenciaRequest request) {
        return Licencia.builder()
                .numero(request.numero().trim())
                .fechaEmision(request.fechaEmision())
                .fechaVencimiento(request.fechaVencimiento())
                .proyecto(buscarProyecto(request.idProyecto()))
                .build();
    }

    @Override
    protected void actualizarEntidad(Licencia licencia, LicenciaRequest request) {
        licencia.setNumero(request.numero().trim());
        licencia.setFechaEmision(request.fechaEmision());
        licencia.setFechaVencimiento(request.fechaVencimiento());
        licencia.setProyecto(buscarProyecto(request.idProyecto()));
    }

    @Override
    protected LicenciaResponse toResponse(Licencia licencia) {
        return LicenciaResponse.from(licencia);
    }

    private Proyecto buscarProyecto(Long idProyecto) {
        return proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proyecto no encontrado con id " + idProyecto));
    }

    private void validarFechas(LicenciaRequest request) {
        if (!request.fechaVencimiento().isAfter(request.fechaEmision())) {
            throw new BadRequestException("La fecha de vencimiento debe ser posterior a la fecha de emisión");
        }
    }
}
