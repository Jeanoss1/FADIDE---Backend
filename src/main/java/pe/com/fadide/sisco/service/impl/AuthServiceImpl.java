package pe.com.fadide.sisco.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.com.fadide.sisco.config.BadRequestException;
import pe.com.fadide.sisco.config.ConflictException;
import pe.com.fadide.sisco.dto.GoogleLoginRequestDTO;
import pe.com.fadide.sisco.dto.LoginRequestDTO;
import pe.com.fadide.sisco.dto.LoginResponseDTO;
import pe.com.fadide.sisco.dto.RegisterRequestDTO;
import pe.com.fadide.sisco.model.Rol;
import pe.com.fadide.sisco.model.Usuario;
import pe.com.fadide.sisco.repository.ClienteRepository;
import pe.com.fadide.sisco.repository.RolRepository;
import pe.com.fadide.sisco.repository.UsuarioRepository;
import pe.com.fadide.sisco.security.GoogleTokenVerifier;
import pe.com.fadide.sisco.security.JwtService;
import pe.com.fadide.sisco.security.UsuarioDetails;
import pe.com.fadide.sisco.service.AuthService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String ROL_CLIENTE = "CLIENTE";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final GoogleTokenVerifier googleTokenVerifier;

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
        return buildResponse(usuarioDetails.getUsuario());
    }

    @Override
    @Transactional
    public LoginResponseDTO register(RegisterRequestDTO dto) {
        String correo = dto.getCorreo().trim().toLowerCase();

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ConflictException("Ya existe una cuenta registrada con ese correo");
        }

        Usuario usuario = crearCliente(dto.getNombreCompleto().trim(), correo, dto.getDocumento(), dto.getPassword());
        // Inicia sesión automáticamente tras el registro
        return buildResponse(usuario);
    }

    @Override
    @Transactional
    public LoginResponseDTO loginWithGoogle(GoogleLoginRequestDTO dto) {
        GoogleTokenVerifier.GoogleUser google = googleTokenVerifier.verify(dto.getCredential());

        // Google es solo para clientes: el personal interno entra con correo y contraseña
        var existente = usuarioRepository.findByCorreo(google.correo());
        if (existente.isPresent()) {
            if (!ROL_CLIENTE.equals(existente.get().getRol().getNombre())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "El acceso con Google es solo para clientes. El personal debe ingresar con correo y contraseña.");
            }
            if (!Boolean.TRUE.equals(existente.get().getActivo())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tu cuenta está desactivada");
            }
            return buildResponse(existente.get());
        }

        // Cuenta nueva: se pide el RUC para validar que sea cliente de FADIDE
        if (dto.getDocumento() == null || dto.getDocumento().isBlank()) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_REQUIRED,
                    "Es tu primer ingreso con Google: ingresa el RUC con el que FADIDE te registró");
        }

        // La contraseña es aleatoria: este usuario entra siempre con Google
        Usuario usuario = crearCliente(google.nombre(), google.correo(), dto.getDocumento(), UUID.randomUUID().toString());
        return buildResponse(usuario);
    }

    private Usuario crearCliente(String nombre, String correo, String documento, String password) {
        // Regla: solo pueden registrarse clientes que la empresa ya dio de alta
        if (clienteRepository.findByRuc(documento).isEmpty()) {
            throw new BadRequestException(
                    "El RUC ingresado no corresponde a ningún cliente de FADIDE. Comunícate con la oficina.");
        }
        if (usuarioRepository.existsByDocumentoAndRol_Nombre(documento, ROL_CLIENTE)) {
            throw new ConflictException("Ya existe una cuenta de cliente para ese RUC");
        }

        Rol rolCliente = rolRepository.findByNombre(ROL_CLIENTE)
                .orElseGet(() -> rolRepository.save(Rol.builder().nombre(ROL_CLIENTE).build()));

        return usuarioRepository.save(Usuario.builder()
                .nombreCompleto(nombre)
                .correo(correo)
                .documento(documento)
                .password(passwordEncoder.encode(password))
                .activo(true)
                .rol(rolCliente)
                .build());
    }

    private LoginResponseDTO buildResponse(Usuario usuario) {
        String rolNombre = usuario.getRol().getNombre();
        String token = jwtService.generateToken(usuario.getCorreo(), rolNombre);

        return LoginResponseDTO.builder()
                .token(token)
                .tipo("Bearer")
                .correo(usuario.getCorreo())
                .nombreCompleto(usuario.getNombreCompleto())
                .rol(rolNombre)
                .build();
    }
}
