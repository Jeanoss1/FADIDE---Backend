package pe.com.fadide.sisco.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public record SupervisionRequest(
        @NotNull(message = "La fecha de la supervisión es obligatoria")
        @PastOrPresent(message = "La fecha de la supervisión no puede ser futura")
        LocalDate fecha,

        @NotNull(message = "El avance es obligatorio")
        @DecimalMin(value = "0.00", message = "El avance no puede ser menor a 0 %")
        @DecimalMax(value = "100.00", message = "El avance no puede superar el 100 %")
        @Digits(integer = 3, fraction = 2, message = "El avance admite hasta 2 decimales")
        BigDecimal avance,

        @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
        String descripcion,

        @NotNull(message = "El contrato es obligatorio")
        Long idContrato
) {
}
