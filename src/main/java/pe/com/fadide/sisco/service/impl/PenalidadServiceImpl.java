package pe.com.fadide.sisco.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pe.com.fadide.sisco.dto.PenalidadRequestDTO;
import pe.com.fadide.sisco.dto.PenalidadResponseDTO;
import pe.com.fadide.sisco.model.Contrato;
import pe.com.fadide.sisco.model.Penalidad;
import pe.com.fadide.sisco.repository.ContratoRepository;
import pe.com.fadide.sisco.repository.PenalidadRepository;
import pe.com.fadide.sisco.service.PenalidadService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PenalidadServiceImpl implements PenalidadService {

    private final PenalidadRepository penalidadRepository;
    private final ContratoRepository contratoRepository;

    @Override
    public List<PenalidadResponseDTO> findAll() {
        return penalidadRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public PenalidadResponseDTO findById(Long id) {
        Penalidad penalidad = penalidadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Penalidad no encontrada con ID: " + id));
        return toResponse(penalidad);
    }

    @Override
    public PenalidadResponseDTO create(PenalidadRequestDTO dto) {
        Contrato contrato = contratoRepository.findById(dto.getIdContrato())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Contrato no encontrado con ID: " + dto.getIdContrato()));

        Penalidad penalidad = Penalidad.builder()
                .montoUit(dto.getMontoUit())
                .fechaAplicacion(dto.getFechaAplicacion())
                .motivo(dto.getMotivo())
                .estado(dto.getEstado() != null ? dto.getEstado() : "APLICADA")
                .contrato(contrato)
                .build();

        return toResponse(penalidadRepository.save(penalidad));
    }

    private PenalidadResponseDTO toResponse(Penalidad p) {
        return PenalidadResponseDTO.builder()
                .idPenalidad(p.getIdPenalidad())
                .montoUit(p.getMontoUit())
                .fechaAplicacion(p.getFechaAplicacion())
                .motivo(p.getMotivo())
                .estado(p.getEstado())
                .idContrato(p.getContrato().getIdContrato())
                .build();
    }
}