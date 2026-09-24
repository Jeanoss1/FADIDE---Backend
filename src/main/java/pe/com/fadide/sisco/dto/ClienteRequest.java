package pe.com.fadide.sisco.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank @Size(max = 150) String razonSocial,
        @NotBlank @Pattern(regexp = "\\d{11}", message = "El RUC debe tener 11 dígitos") String ruc,
        @Size(max = 200) String direccion,
        @Size(max = 20) String telefono
) {
}
