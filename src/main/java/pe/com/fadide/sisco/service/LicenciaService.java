package pe.com.fadide.sisco.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.com.fadide.sisco.config.BadRequestException;
import pe.com.fadide.sisco.config.ConflictException;
import pe.com.fadide.sisco.config.ResourceNotFoundException;
import pe.com.fadide.sisco.dto.LicenciaRequest;
import pe.com.fadide.sisco.dto.LicenciaResponse;
import pe.com.fadide.sisco.model.Licencia;
import pe.com.fadide.sisco.model.Proyecto;
import pe.com.fadide.sisco.repository.LicenciaRepository;
import pe.com.fadide.sisco.repository.ProyectoRepository;

// Patrón Singleton: bean de Spring con una única instancia
@Service
public class LicenciaService {

    private final LicenciaRepository licenciaRepository;
    private final ProyectoRepository proyectoRepository;

    public LicenciaService(LicenciaRepository licenciaRepository, ProyectoRepository proyectoRepository) {
        this.licenciaRepository = licenciaRepository;
        this.proyectoRepository = proyectoRepository;
    }

    @Transactional(readOnly = true)
    public List<LicenciaResponse> listar() {
        return licenciaRepository.findAll().stream().map(LicenciaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public LicenciaResponse obtener(Long id) {
        return LicenciaResponse.from(buscar(id));
    }

    @Transactional(readOnly = true)
    public LicenciaResponse obtenerPorProyecto(Long idProyecto) {
        return licenciaRepository.buscarPorProyecto(idProyecto)
                .map(LicenciaResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe una licencia para el proyecto con id " + idProyecto));
    }

    @Transactional(readOnly = true)
    public List<LicenciaResponse> listarPorVencer(int dias) {
        if (dias < 0) {
            throw new BadRequestException("El número de días no puede ser negativo");
        }
        LocalDate hoy = LocalDate.now();
        return licenciaRepository.buscarPorVencer(hoy, hoy.plusDays(dias)).stream()
                .map(LicenciaResponse::from)
                .toList();
    }

    @Transactional
    public LicenciaResponse crear(LicenciaRequest request) {
        validarFechas(request);
        if (licenciaRepository.existsByNumero(request.numero())) {
            throw new ConflictException("Ya existe una licencia con el número " + request.numero());
        }
        if (licenciaRepository.existsByProyectoId(request.idProyecto())) {
            throw new ConflictException("El proyecto ya tiene una licencia asociada");
        }
        Licencia licencia = Licencia.builder()
                .numero(request.numero().trim())
                .fechaEmision(request.fechaEmision())
                .fechaVencimiento(request.fechaVencimiento())
                .proyecto(buscarProyecto(request.idProyecto()))
                .build();
        return LicenciaResponse.from(licenciaRepository.save(licencia));
    }

    @Transactional
    public LicenciaResponse actualizar(Long id, LicenciaRequest request) {
        Licencia licencia = buscar(id);
        validarFechas(request);
        if (licenciaRepository.existsByNumeroAndIdLicenciaNot(request.numero(), id)) {
            throw new ConflictException("Ya existe una licencia con el número " + request.numero());
        }
        if (licenciaRepository.existsByProyectoIdAndIdLicenciaNot(request.idProyecto(), id)) {
            throw new ConflictException("El proyecto ya tiene una licencia asociada");
        }
        licencia.setNumero(request.numero().trim());
        licencia.setFechaEmision(request.fechaEmision());
        licencia.setFechaVencimiento(request.fechaVencimiento());
        licencia.setProyecto(buscarProyecto(request.idProyecto()));
        return LicenciaResponse.from(licenciaRepository.save(licencia));
    }

    @Transactional
    public void eliminar(Long id) {
        licenciaRepository.delete(buscar(id));
    }

    private Licencia buscar(Long id) {
        return licenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Licencia no encontrada con id " + id));
    }

    private Proyecto buscarProyecto(Long idProyecto) {
        return proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con id " + idProyecto));
    }

    private void validarFechas(LicenciaRequest request) {
        if (!request.fechaVencimiento().isAfter(request.fechaEmision())) {
            throw new BadRequestException("La fecha de vencimiento debe ser posterior a la fecha de emisión");
        }
    }
}
