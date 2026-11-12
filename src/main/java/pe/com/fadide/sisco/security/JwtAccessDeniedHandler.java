package pe.com.fadide.sisco.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Se invoca cuando el usuario SÍ está autenticado (token válido) pero su rol
 * no tiene permiso sobre el recurso. Responde 403 en JSON.
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        SecurityErrorWriter.write(response, HttpStatus.FORBIDDEN,
                "Tu rol no tiene permisos para acceder a este recurso", request.getRequestURI());
    }
}
