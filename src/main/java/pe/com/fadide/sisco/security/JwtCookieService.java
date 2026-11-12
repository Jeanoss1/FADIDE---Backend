package pe.com.fadide.sisco.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Enfoque HttpOnly: el token viaja en una cookie que JavaScript no puede leer
 * (protege contra robo del token por XSS). El navegador la envía sola en cada
 * petición al backend cuando el frontend usa withCredentials.
 */
@Component
@RequiredArgsConstructor
public class JwtCookieService {

    public static final String COOKIE_NAME = "SISCO_TOKEN";

    private final JwtService jwtService;

    // En producción (HTTPS) debe ser true para que la cookie solo viaje cifrada
    @Value("${sisco.jwt.cookie-secure:false}")
    private boolean secure;

    public ResponseCookie crear(String token) {
        return base(token)
                .maxAge(Duration.ofMillis(jwtService.getExpirationMs()))
                .build();
    }

    // Cookie vacía con maxAge 0: el navegador la elimina (logout)
    public ResponseCookie eliminar() {
        return base("").maxAge(0).build();
    }

    public String leer(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName()) && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private ResponseCookie.ResponseCookieBuilder base(String valor) {
        return ResponseCookie.from(COOKIE_NAME, valor)
                .httpOnly(true)      // no accesible desde document.cookie
                .secure(secure)      // solo HTTPS cuando es true
                .sameSite("Lax")     // no se envía en peticiones POST de otros sitios (mitiga CSRF)
                .path("/");
    }
}
