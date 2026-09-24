package pe.com.fadide.sisco.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {
    private String token;
    private String tipo;
    private String correo;
    private String nombreCompleto;
    private String rol;
}
