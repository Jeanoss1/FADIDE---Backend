package pe.com.fadide.sisco.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LicenciaRequest(
        @NotBlank(message = "El número de licencia es obligatorio")
        @Size(max = 50, message = "El número no puede superar 50 caracteres")
        String numero,

        @NotNull(message = "La fecha de emisión es obligatoria")
        LocalDate fechaEmision,

        @NotNull(message = "La fecha de vencimiento es obligatoria")
        LocalDate fechaVencimiento,

        @NotNull(message = "La obra es obligatoria")
        Long idProyecto
) {
    // Validación entre campos: se reporta como error del campo "fechasValidas"
    @AssertTrue(message = "La fecha de vencimiento debe ser posterior a la fecha de emisión")
    public boolean isFechasValidas() {
        return fechaEmision == null || fechaVencimiento == null || fechaVencimiento.isAfter(fechaEmision);
    }
}
