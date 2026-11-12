package pe.com.fadide.sisco.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Validación del DTO: se ejecuta con @Valid en el controlador, antes de llegar al servicio
public record ClienteRequest(
        @NotBlank(message = "La razón social es obligatoria")
        @Size(min = 3, max = 150, message = "La razón social debe tener entre 3 y 150 caracteres")
        String razonSocial,

        @NotBlank(message = "El RUC es obligatorio")
        @Pattern(regexp = "(10|15|17|20)\\d{9}", message = "El RUC debe tener 11 dígitos y empezar con 10, 15, 17 o 20")
        String ruc,

        @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
        String direccion,

        @Pattern(regexp = "^$|9\\d{8}", message = "El teléfono debe tener 9 dígitos y empezar con 9")
        String telefono
) {
}
