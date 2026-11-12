package pe.com.fadide.sisco.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleLoginRequestDTO {

    // ID token que entrega Google Identity Services en el navegador
    @NotBlank(message = "El token de Google es obligatorio")
    private String credential;

    // Solo se envía la primera vez, para crear la cuenta de cliente
    @Pattern(regexp = "\\d{11}", message = "El RUC debe tener 11 dígitos")
    private String documento;
}
