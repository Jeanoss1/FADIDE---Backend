package pe.com.fadide.sisco.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.com.fadide.sisco.dto.GoogleLoginRequestDTO;
import pe.com.fadide.sisco.dto.LoginRequestDTO;
import pe.com.fadide.sisco.dto.LoginResponseDTO;
import pe.com.fadide.sisco.dto.RegisterRequestDTO;
import pe.com.fadide.sisco.dto.SesionResponseDTO;
import pe.com.fadide.sisco.security.JwtCookieService;
import pe.com.fadide.sisco.security.UsuarioDetails;
import pe.com.fadide.sisco.service.AuthService;

/**
 * Autenticación. Cada login devuelve el token de DOS formas:
 *  1. En el cuerpo JSON (enfoque Authorization: Bearer, lo usa el interceptor de Angular).
 *  2. En una cookie HttpOnly (enfoque HttpOnly: el navegador la guarda y la envía sola).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtCookieService jwtCookieService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return conCookie(HttpStatus.OK, authService.login(dto));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        return conCookie(HttpStatus.CREATED, authService.register(dto));
    }

    @PostMapping("/google")
    public ResponseEntity<LoginResponseDTO> google(@Valid @RequestBody GoogleLoginRequestDTO dto) {
        return conCookie(HttpStatus.OK, authService.loginWithGoogle(dto));
    }

    // Comprobante de inicio de sesión: devuelve el usuario dueño del token (401 si no es válido)
    @GetMapping("/me")
    public ResponseEntity<SesionResponseDTO> me(@AuthenticationPrincipal UsuarioDetails usuario) {
        return ResponseEntity.ok(new SesionResponseDTO(
                usuario.getUsername(),
                usuario.getUsuario().getNombreCompleto(),
                usuario.getUsuario().getRol().getNombre()));
    }

    // Cierra la sesión eliminando la cookie HttpOnly (el token Bearer lo descarta el frontend)
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, jwtCookieService.eliminar().toString())
                .build();
    }

    private ResponseEntity<LoginResponseDTO> conCookie(HttpStatus status, LoginResponseDTO body) {
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, jwtCookieService.crear(body.getToken()).toString())
                .body(body);
    }
}
