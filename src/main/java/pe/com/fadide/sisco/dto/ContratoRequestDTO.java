package pe.com.fadide.sisco.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratoRequestDTO {

    @NotBlank(message = "El número de contrato es obligatorio")
    @Size(max = 50, message = "El número de contrato no puede superar 50 caracteres")
    private String numeroContrato;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    @NotNull(message = "El monto total es obligatorio")
    @Positive(message = "El monto total debe ser mayor que cero")
    @Digits(integer = 10, fraction = 2, message = "El monto total admite hasta 10 enteros y 2 decimales")
    private BigDecimal montoTotal;

    @Size(max = 30, message = "El estado no puede superar 30 caracteres")
    private String estado;

    @NotNull(message = "El proyecto es obligatorio")
    private Long idProyecto;
}
