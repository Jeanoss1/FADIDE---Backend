package pe.com.fadide.sisco.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public record EvidenciaRequest(
        @NotBlank(message = "La imagen (URL) de la evidencia es obligatoria")
        @Size(max = 500, message = "La URL de la imagen no puede superar 500 caracteres")
        String imagen,

        @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
        String descripcion,

        @NotNull(message = "La fecha es obligatoria")
        @PastOrPresent(message = "La fecha no puede ser futura")
        LocalDate fecha,

        @NotNull(message = "La supervisión es obligatoria")
        Long idSupervision
) {
}
