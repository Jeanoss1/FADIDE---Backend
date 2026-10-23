package pe.com.fadide.sisco.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import pe.com.fadide.sisco.common.exception.RecursoNoEncontradoException;
import pe.com.fadide.sisco.common.service.impl.CrudServiceImpl;
import pe.com.fadide.sisco.dto.PenalidadRequestDTO;
import pe.com.fadide.sisco.dto.PenalidadResponseDTO;
import pe.com.fadide.sisco.model.Contrato;
import pe.com.fadide.sisco.model.Penalidad;
import pe.com.fadide.sisco.repository.ContratoRepository;
import pe.com.fadide.sisco.repository.PenalidadRepository;
import pe.com.fadide.sisco.service.PenalidadService;

@Service
@RequiredArgsConstructor
public class PenalidadServiceImpl extends CrudServiceImpl<Penalidad, PenalidadRequestDTO, PenalidadResponseDTO, Long>
        implements PenalidadService {

    private final PenalidadRepository penalidadRepository;
    private final ContratoRepository contratoRepository;

    @Override
    protected JpaRepository<Penalidad, Long> getRepository() {
        return penalidadRepository;
    }

    @Override
    protected String mensajeNoEncontrado(Long id) {
        return "Penalidad no encontrada con ID: " + id;
    }

    @Override
    protected Penalidad toEntity(PenalidadRequestDTO dto) {
        return Penalidad.builder()
                .montoUit(dto.getMontoUit())
                .fechaAplicacion(dto.getFechaAplicacion())
                .motivo(dto.getMotivo())
                .estado(dto.getEstado() != null ? dto.getEstado() : "APLICADA")
                .contrato(buscarContrato(dto.getIdContrato()))
                .build();
    }

    // El controller no expone PUT (el backend solo registra penalidades); se implementa por el contrato genérico
    @Override
    protected void actualizarEntidad(Penalidad penalidad, PenalidadRequestDTO dto) {
        penalidad.setMontoUit(dto.getMontoUit());
        penalidad.setFechaAplicacion(dto.getFechaAplicacion());
        penalidad.setMotivo(dto.getMotivo());
        if (dto.getEstado() != null) {
            penalidad.setEstado(dto.getEstado());
        }
        penalidad.setContrato(buscarContrato(dto.getIdContrato()));
    }

    @Override
    protected PenalidadResponseDTO toResponse(Penalidad p) {
        return PenalidadResponseDTO.builder()
                .idPenalidad(p.getIdPenalidad())
                .montoUit(p.getMontoUit())
                .fechaAplicacion(p.getFechaAplicacion())
                .motivo(p.getMotivo())
                .estado(p.getEstado())
                .idContrato(p.getContrato().getIdContrato())
                .build();
    }

    private Contrato buscarContrato(Long idContrato) {
        return contratoRepository.findById(idContrato)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Contrato no encontrado con ID: " + idContrato));
    }
}
