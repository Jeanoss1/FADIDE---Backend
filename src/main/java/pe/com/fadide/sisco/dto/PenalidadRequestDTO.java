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
public class PenalidadRequestDTO {

    @NotNull(message = "El monto en UIT es obligatorio")
    @Positive(message = "El monto en UIT debe ser mayor que cero")
    @Digits(integer = 6, fraction = 4, message = "El monto en UIT admite hasta 6 enteros y 4 decimales")
    private BigDecimal montoUit;

    @NotNull(message = "La fecha de aplicación es obligatoria")
    private LocalDate fechaAplicacion;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 255, message = "El motivo no puede superar 255 caracteres")
    private String motivo;

    @Size(max = 30, message = "El estado no puede superar 30 caracteres")
    private String estado;

    @NotNull(message = "El contrato es obligatorio")
    private Long idContrato;
}
