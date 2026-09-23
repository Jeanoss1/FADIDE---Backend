package pe.com.fadide.sisco.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pe.com.fadide.sisco.dto.PagoRequestDTO;
import pe.com.fadide.sisco.dto.PagoResponseDTO;
import pe.com.fadide.sisco.model.Contrato;
import pe.com.fadide.sisco.model.Pago;
import pe.com.fadide.sisco.repository.ContratoRepository;
import pe.com.fadide.sisco.repository.PagoRepository;
import pe.com.fadide.sisco.service.PagoService;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final ContratoRepository contratoRepository;

    @Override
    public List<PagoResponseDTO> findAll() {
        return pagoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public PagoResponseDTO findById(Long id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Pago no encontrado con ID: " + id));
        return toResponse(pago);
    }

    @Override
    public PagoResponseDTO create(PagoRequestDTO dto) {
        Contrato contrato = contratoRepository.findById(dto.getIdContrato())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Contrato no encontrado con ID: " + dto.getIdContrato()));

        // Regla: la suma de los pagos no puede superar el monto total del contrato
        BigDecimal pagosPrevios = pagoRepository.findByContrato_IdContrato(contrato.getIdContrato())
                .stream()
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalConEsteNuevo = pagosPrevios.add(dto.getMonto());

        if (totalConEsteNuevo.compareTo(contrato.getMontoTotal()) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El pago excede el monto total del contrato (monto total: "
                            + contrato.getMontoTotal() + ", ya pagado: " + pagosPrevios
                            + ", intentado: " + dto.getMonto() + ")");
        }

        Pago pago = Pago.builder()
                .monto(dto.getMonto())
                .fechaPago(dto.getFechaPago())
                .estado(dto.getEstado() != null ? dto.getEstado() : "REGISTRADO")
                .conformidadEmitida(dto.getConformidadEmitida() != null ? dto.getConformidadEmitida() : false)
                .contrato(contrato)
                .build();

        return toResponse(pagoRepository.save(pago));
    }

    private PagoResponseDTO toResponse(Pago p) {
        return PagoResponseDTO.builder()
                .idPago(p.getIdPago())
                .monto(p.getMonto())
                .fechaPago(p.getFechaPago())
                .estado(p.getEstado())
                .conformidadEmitida(p.getConformidadEmitida())
                .idContrato(p.getContrato().getIdContrato())
                .build();
    }
}