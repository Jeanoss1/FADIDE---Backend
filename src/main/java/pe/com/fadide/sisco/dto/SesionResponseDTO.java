package pe.com.fadide.sisco.dto;

// Comprobante de sesión: datos del usuario dueño del token (GET /api/auth/me)
public record SesionResponseDTO(
        String correo,
        String nombreCompleto,
        String rol
) {
}
