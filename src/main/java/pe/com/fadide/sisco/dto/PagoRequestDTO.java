package pe.com.fadide.sisco.dto;

import jakarta.validation.constraints.Digits;
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
public class PagoRequestDTO {

    // Regla: un pago negativo "restaría" saldo y permitiría pagar más que el monto del contrato
    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto del pago debe ser mayor que cero")
    @Digits(integer = 10, fraction = 2, message = "El monto admite hasta 10 enteros y 2 decimales")
    private BigDecimal monto;

    @NotNull(message = "La fecha de pago es obligatoria")
    private LocalDate fechaPago;

    @Size(max = 30, message = "El estado no puede superar 30 caracteres")
    private String estado;

    private Boolean conformidadEmitida;

    @NotNull(message = "El contrato es obligatorio")
    private Long idContrato;
}
