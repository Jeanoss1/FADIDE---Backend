package pe.com.fadide.sisco.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pe.com.fadide.sisco.common.exception.RecursoNoEncontradoException;
import pe.com.fadide.sisco.common.service.impl.CrudServiceImpl;
import pe.com.fadide.sisco.dto.PagoRequestDTO;
import pe.com.fadide.sisco.dto.PagoResponseDTO;
import pe.com.fadide.sisco.model.Contrato;
import pe.com.fadide.sisco.model.Pago;
import pe.com.fadide.sisco.repository.ContratoRepository;
import pe.com.fadide.sisco.repository.PagoRepository;
import pe.com.fadide.sisco.service.PagoService;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl extends CrudServiceImpl<Pago, PagoRequestDTO, PagoResponseDTO, Long>
        implements PagoService {

    private final PagoRepository pagoRepository;
    private final ContratoRepository contratoRepository;

    @Override
    protected JpaRepository<Pago, Long> getRepository() {
        return pagoRepository;
    }

    @Override
    protected String mensajeNoEncontrado(Long id) {
        return "Pago no encontrado con ID: " + id;
    }

    @Override
    protected Pago toEntity(PagoRequestDTO dto) {
        Contrato contrato = buscarContrato(dto.getIdContrato());

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

        return Pago.builder()
                .monto(dto.getMonto())
                .fechaPago(dto.getFechaPago())
                .estado(dto.getEstado() != null ? dto.getEstado() : "REGISTRADO")
                .conformidadEmitida(dto.getConformidadEmitida() != null ? dto.getConformidadEmitida() : false)
                .contrato(contrato)
                .build();
    }

    // El controller no expone PUT (el backend solo registra pagos); se implementa por el contrato genérico
    @Override
    protected void actualizarEntidad(Pago pago, PagoRequestDTO dto) {
        pago.setMonto(dto.getMonto());
        pago.setFechaPago(dto.getFechaPago());
        if (dto.getEstado() != null) {
            pago.setEstado(dto.getEstado());
        }
        if (dto.getConformidadEmitida() != null) {
            pago.setConformidadEmitida(dto.getConformidadEmitida());
        }
        pago.setContrato(buscarContrato(dto.getIdContrato()));
    }

    @Override
    protected PagoResponseDTO toResponse(Pago p) {
        return PagoResponseDTO.builder()
                .idPago(p.getIdPago())
                .monto(p.getMonto())
                .fechaPago(p.getFechaPago())
                .estado(p.getEstado())
                .conformidadEmitida(p.getConformidadEmitida())
                .idContrato(p.getContrato().getIdContrato())
                .build();
    }

    private Contrato buscarContrato(Long idContrato) {
        return contratoRepository.findById(idContrato)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Contrato no encontrado con ID: " + idContrato));
    }
}
