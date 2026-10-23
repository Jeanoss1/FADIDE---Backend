package pe.com.fadide.sisco.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pe.com.fadide.sisco.common.exception.RecursoNoEncontradoException;
import pe.com.fadide.sisco.common.service.impl.CrudServiceImpl;
import pe.com.fadide.sisco.dto.ContratoRequestDTO;
import pe.com.fadide.sisco.dto.ContratoResponseDTO;
import pe.com.fadide.sisco.model.Contrato;
import pe.com.fadide.sisco.model.Proyecto;
import pe.com.fadide.sisco.repository.ContratoRepository;
import pe.com.fadide.sisco.repository.PagoRepository;
import pe.com.fadide.sisco.repository.PenalidadRepository;
import pe.com.fadide.sisco.repository.ProyectoRepository;
import pe.com.fadide.sisco.repository.SupervisionRepository;
import pe.com.fadide.sisco.service.ContratoService;

@Service
@RequiredArgsConstructor
public class ContratoServiceImpl extends CrudServiceImpl<Contrato, ContratoRequestDTO, ContratoResponseDTO, Long>
        implements ContratoService {

    private final ContratoRepository contratoRepository;
    private final ProyectoRepository proyectoRepository;
    private final PagoRepository pagoRepository;
    private final PenalidadRepository penalidadRepository;
    private final SupervisionRepository supervisionRepository;

    @Override
    protected JpaRepository<Contrato, Long> getRepository() {
        return contratoRepository;
    }

    @Override
    protected String mensajeNoEncontrado(Long id) {
        return "Contrato no encontrado con ID: " + id;
    }

    @Override
    protected void validarCrear(ContratoRequestDTO dto) {
        // Regla: la fecha de fin no puede ser anterior a la fecha de inicio
        if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        // Regla: el numero de contrato es unico
        if (contratoRepository.existsByNumeroContrato(dto.getNumeroContrato())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un contrato con el numero: " + dto.getNumeroContrato());
        }
    }

    @Override
    protected void validarEliminar(Long id, Contrato contrato) {
        // Regla: no se puede eliminar un contrato con pagos asociados
        if (pagoRepository.existsByContrato_IdContrato(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar: el contrato tiene pagos registrados");
        }
        // Regla: no se puede eliminar un contrato con penalidades asociadas
        if (penalidadRepository.existsByContrato_IdContrato(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar: el contrato tiene penalidades registradas");
        }
        // Regla: no se puede eliminar un contrato con una supervision asociada
        if (supervisionRepository.existsByContratoId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar: el contrato tiene una supervision registrada");
        }
    }

    @Override
    protected Contrato toEntity(ContratoRequestDTO dto) {
        return Contrato.builder()
                .numeroContrato(dto.getNumeroContrato())
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .montoTotal(dto.getMontoTotal())
                .estado(dto.getEstado() != null ? dto.getEstado() : "VIGENTE")
                .proyecto(buscarProyecto(dto.getIdProyecto()))
                .build();
    }

    // El controller no expone PUT (el backend solo registra contratos); se implementa por el contrato genérico
    @Override
    protected void actualizarEntidad(Contrato contrato, ContratoRequestDTO dto) {
        contrato.setNumeroContrato(dto.getNumeroContrato());
        contrato.setFechaInicio(dto.getFechaInicio());
        contrato.setFechaFin(dto.getFechaFin());
        contrato.setMontoTotal(dto.getMontoTotal());
        if (dto.getEstado() != null) {
            contrato.setEstado(dto.getEstado());
        }
        contrato.setProyecto(buscarProyecto(dto.getIdProyecto()));
    }

    @Override
    protected ContratoResponseDTO toResponse(Contrato c) {
        return ContratoResponseDTO.builder()
                .idContrato(c.getIdContrato())
                .numeroContrato(c.getNumeroContrato())
                .fechaInicio(c.getFechaInicio())
                .fechaFin(c.getFechaFin())
                .montoTotal(c.getMontoTotal())
                .estado(c.getEstado())
                .idProyecto(c.getProyecto().getIdProyecto())
                .build();
    }

    private Proyecto buscarProyecto(Long idProyecto) {
        return proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Proyecto no encontrado con ID: " + idProyecto));
    }
}
