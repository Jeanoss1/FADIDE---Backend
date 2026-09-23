package pe.com.fadide.sisco.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PenalidadResponseDTO {
    private Long idPenalidad;
    private BigDecimal montoUit;
    private LocalDate fechaAplicacion;
    private String motivo;
    private String estado;
    private Long idContrato;
}