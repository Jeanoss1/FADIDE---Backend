package pe.com.fadide.sisco.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Se invoca cuando una petición a un recurso protegido llega SIN autenticación válida
 * (sin token, token expirado, firma inválida...). Responde 401 en JSON con el mismo
 * formato que GlobalExceptionHandler, en lugar del 403 vacío que Spring envía por defecto.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    public static final String ATTR_ERROR = "jwt_error";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        Object motivo = request.getAttribute(ATTR_ERROR);
        String message = motivo != null
                ? motivo.toString()
                : "No se encontró el token de acceso: inicia sesión para continuar";

        SecurityErrorWriter.write(response, HttpStatus.UNAUTHORIZED, message, request.getRequestURI());
    }
}
