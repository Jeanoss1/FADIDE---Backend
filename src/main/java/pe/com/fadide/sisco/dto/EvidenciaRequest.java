package pe.com.fadide.sisco.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public record EvidenciaRequest(
        @NotBlank @Size(max = 500) String imagen,
        @Size(max = 1000) String descripcion,
        @NotNull @PastOrPresent LocalDate fecha,
        @NotNull Long idSupervision
) {
}
