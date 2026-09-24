package pe.com.fadide.sisco.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ContratistaRequest(
        @NotBlank @Size(max = 150) String nombre,
        @NotBlank @Pattern(regexp = "\\d{11}", message = "El RUC debe tener 11 dígitos") String ruc
) {
}
