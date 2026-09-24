package pe.com.fadide.sisco.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pe.com.fadide.sisco.dto.UsuarioRequestDTO;
import pe.com.fadide.sisco.dto.UsuarioResponseDTO;
import pe.com.fadide.sisco.model.Rol;
import pe.com.fadide.sisco.model.Usuario;
import pe.com.fadide.sisco.repository.RolRepository;
import pe.com.fadide.sisco.repository.UsuarioRepository;
import pe.com.fadide.sisco.service.UsuarioService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UsuarioResponseDTO> findAll() {
        return usuarioRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UsuarioResponseDTO create(UsuarioRequestDTO dto) {
        // Regla: el correo de cada usuario es unico
        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un usuario registrado con el correo: " + dto.getCorreo());
        }

        Rol rol = rolRepository.findById(dto.getIdRol())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Rol no encontrado con ID: " + dto.getIdRol()));

        // Regla: las contraseñas se guardan encriptadas (BCrypt), nunca en texto plano
        Usuario usuario = Usuario.builder()
                .nombreCompleto(dto.getNombreCompleto())
                .correo(dto.getCorreo())
                .documento(dto.getDocumento())
                .password(passwordEncoder.encode(dto.getPassword()))
                .activo(true)
                .rol(rol)
                .build();

        return toResponse(usuarioRepository.save(usuario));
    }

    private UsuarioResponseDTO toResponse(Usuario u) {
        return UsuarioResponseDTO.builder()
                .idUsuario(u.getIdUsuario())
                .nombreCompleto(u.getNombreCompleto())
                .correo(u.getCorreo())
                .documento(u.getDocumento())
                .activo(u.getActivo())
                .idRol(u.getRol().getIdRol())
                .nombreRol(u.getRol().getNombre())
                .build();
    }
}
