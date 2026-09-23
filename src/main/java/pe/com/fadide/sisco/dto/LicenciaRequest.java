package pe.com.fadide.sisco.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LicenciaRequest(
        @NotBlank @Size(max = 50) String numero,
        @NotNull LocalDate fechaEmision,
        @NotNull LocalDate fechaVencimiento,
        @NotNull Long idProyecto
) {
}
