package pe.com.fadide.sisco.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ContratistaRequest(
        @NotBlank(message = "El nombre del contratista es obligatorio")
        @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
        String nombre,

        @NotBlank(message = "El RUC es obligatorio")
        @Pattern(regexp = "(10|15|17|20)\\d{9}", message = "El RUC debe tener 11 dígitos y empezar con 10, 15, 17 o 20")
        String ruc
) {
}
