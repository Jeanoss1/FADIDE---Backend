package pe.com.fadide.sisco.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pe.com.fadide.sisco.dto.LoginRequestDTO;
import pe.com.fadide.sisco.dto.LoginResponseDTO;
import pe.com.fadide.sisco.security.JwtService;
import pe.com.fadide.sisco.security.UsuarioDetails;
import pe.com.fadide.sisco.service.AuthService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public LoginResponseDTO login(LoginRequestDTO dto) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getCorreo(), dto.getPassword()));
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
        }

        UsuarioDetails usuarioDetails = (UsuarioDetails) authentication.getPrincipal();
        String rolNombre = usuarioDetails.getUsuario().getRol().getNombre();
        String token = jwtService.generateToken(usuarioDetails.getUsername(), rolNombre);

        return LoginResponseDTO.builder()
                .token(token)
                .tipo("Bearer")
                .correo(usuarioDetails.getUsername())
                .nombreCompleto(usuarioDetails.getUsuario().getNombreCompleto())
                .rol(rolNombre)
                .build();
    }
}
