package pe.com.fadide.sisco.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pe.com.fadide.sisco.common.exception.RecursoNoEncontradoException;
import pe.com.fadide.sisco.common.service.impl.CrudServiceImpl;
import pe.com.fadide.sisco.dto.UsuarioRequestDTO;
import pe.com.fadide.sisco.dto.UsuarioResponseDTO;
import pe.com.fadide.sisco.model.Rol;
import pe.com.fadide.sisco.model.Usuario;
import pe.com.fadide.sisco.repository.RolRepository;
import pe.com.fadide.sisco.repository.UsuarioRepository;
import pe.com.fadide.sisco.service.UsuarioService;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl extends CrudServiceImpl<Usuario, UsuarioRequestDTO, UsuarioResponseDTO, Long>
        implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    protected JpaRepository<Usuario, Long> getRepository() {
        return usuarioRepository;
    }

    @Override
    protected String mensajeNoEncontrado(Long id) {
        return "Usuario no encontrado con ID: " + id;
    }

    @Override
    protected void validarCrear(UsuarioRequestDTO dto) {
        // Regla: el correo de cada usuario es unico
        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un usuario registrado con el correo: " + dto.getCorreo());
        }
    }

    @Override
    protected Usuario toEntity(UsuarioRequestDTO dto) {
        // Regla: las contraseñas se guardan encriptadas (BCrypt), nunca en texto plano
        return Usuario.builder()
                .nombreCompleto(dto.getNombreCompleto())
                .correo(dto.getCorreo())
                .documento(dto.getDocumento())
                .password(passwordEncoder.encode(dto.getPassword()))
                .activo(true)
                .rol(buscarRol(dto.getIdRol()))
                .build();
    }

    // El controller no expone PUT (solo listar y crear); se implementa por el contrato genérico
    @Override
    protected void actualizarEntidad(Usuario usuario, UsuarioRequestDTO dto) {
        usuario.setNombreCompleto(dto.getNombreCompleto());
        usuario.setCorreo(dto.getCorreo());
        usuario.setDocumento(dto.getDocumento());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRol(buscarRol(dto.getIdRol()));
    }

    @Override
    protected UsuarioResponseDTO toResponse(Usuario u) {
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

    private Rol buscarRol(Long idRol) {
        return rolRepository.findById(idRol)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado con ID: " + idRol));
    }
}
