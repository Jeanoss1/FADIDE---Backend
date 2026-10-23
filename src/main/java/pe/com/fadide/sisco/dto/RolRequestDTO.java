package pe.com.fadide.sisco.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

// DTO de entrada del rol (lo usa el CRUD genérico; hoy no hay endpoint que lo reciba)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolRequestDTO {
    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(max = 50, message = "El nombre del rol no puede superar 50 caracteres")
    private String nombre;
}
