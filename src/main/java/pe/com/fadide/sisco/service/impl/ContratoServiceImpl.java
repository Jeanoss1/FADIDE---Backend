package pe.com.fadide.sisco.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pe.com.fadide.sisco.dto.ContratoRequestDTO;
import pe.com.fadide.sisco.dto.ContratoResponseDTO;
import pe.com.fadide.sisco.model.Contrato;
import pe.com.fadide.sisco.model.Proyecto;
import pe.com.fadide.sisco.repository.ContratoRepository;
import pe.com.fadide.sisco.repository.PagoRepository;
import pe.com.fadide.sisco.repository.PenalidadRepository;
import pe.com.fadide.sisco.repository.ProyectoRepository;
import pe.com.fadide.sisco.service.ContratoService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContratoServiceImpl implements ContratoService {

    private final ContratoRepository contratoRepository;
    private final ProyectoRepository proyectoRepository;
    private final PagoRepository pagoRepository;
    private final PenalidadRepository penalidadRepository;

    @Override
    public List<ContratoResponseDTO> findAll() {
        return contratoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ContratoResponseDTO findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Override
    public ContratoResponseDTO create(ContratoRequestDTO dto) {
        // Regla: la fecha de fin no puede ser anterior a la fecha de inicio
        if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        Proyecto proyecto = proyectoRepository.findById(dto.getIdProyecto())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Proyecto no encontrado con ID: " + dto.getIdProyecto()));

        Contrato contrato = Contrato.builder()
                .numeroContrato(dto.getNumeroContrato())
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .montoTotal(dto.getMontoTotal())
                .estado(dto.getEstado() != null ? dto.getEstado() : "VIGENTE")
                .proyecto(proyecto)
                .build();

        return toResponse(contratoRepository.save(contrato));
    }

    @Override
    public void deleteById(Long id) {
        Contrato contrato = getOrThrow(id);

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

        contratoRepository.delete(contrato);
    }

    private Contrato getOrThrow(Long id) {
        return contratoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Contrato no encontrado con ID: " + id));
    }

    private ContratoResponseDTO toResponse(Contrato c) {
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
}