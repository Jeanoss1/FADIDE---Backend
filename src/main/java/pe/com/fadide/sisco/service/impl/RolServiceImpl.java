package pe.com.fadide.sisco.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pe.com.fadide.sisco.dto.RolResponseDTO;
import pe.com.fadide.sisco.model.Rol;
import pe.com.fadide.sisco.repository.RolRepository;
import pe.com.fadide.sisco.repository.UsuarioRepository;
import pe.com.fadide.sisco.service.RolService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public List<RolResponseDTO> findAll() {
        return rolRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Rol no encontrado con ID: " + id));

        // Regla: no se puede eliminar un rol que todavia tiene usuarios asignados
        if (usuarioRepository.existsByRol_IdRol(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar: el rol tiene usuarios asignados");
        }

        rolRepository.delete(rol);
    }

    private RolResponseDTO toResponse(Rol r) {
        return RolResponseDTO.builder()
                .idRol(r.getIdRol())
                .nombre(r.getNombre())
                .build();
    }
}
