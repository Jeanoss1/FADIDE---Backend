package pe.com.fadide.sisco.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {
    private Long idUsuario;
    private String nombreCompleto;
    private String correo;
    private String documento;
    private Boolean activo;
    private Long idRol;
    private String nombreRol;
}
