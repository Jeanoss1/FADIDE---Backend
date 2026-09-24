package pe.com.fadide.sisco.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRequestDTO {
    private String nombreCompleto;
    private String correo;
    private String documento;
    private String password;
    private Long idRol;
}
