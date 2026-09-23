package pe.com.fadide.sisco.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoResponseDTO {
    private Long idPago;
    private BigDecimal monto;
    private LocalDate fechaPago;
    private String estado;
    private Boolean conformidadEmitida;
    private Long idContrato;
}