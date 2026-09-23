package pe.com.fadide.sisco.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratoResponseDTO {
    private Long idContrato;
    private String numeroContrato;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal montoTotal;
    private String estado;
    private Long idProyecto;
}