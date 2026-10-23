package pe.com.fadide.sisco.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pe.com.fadide.sisco.common.service.impl.CrudServiceImpl;
import pe.com.fadide.sisco.dto.RolRequestDTO;
import pe.com.fadide.sisco.dto.RolResponseDTO;
import pe.com.fadide.sisco.model.Rol;
import pe.com.fadide.sisco.repository.RolRepository;
import pe.com.fadide.sisco.repository.UsuarioRepository;
import pe.com.fadide.sisco.service.RolService;

@Service
@RequiredArgsConstructor
public class RolServiceImpl extends CrudServiceImpl<Rol, RolRequestDTO, RolResponseDTO, Long>
        implements RolService {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected JpaRepository<Rol, Long> getRepository() {
        return rolRepository;
    }

    @Override
    protected String mensajeNoEncontrado(Long id) {
        return "Rol no encontrado con ID: " + id;
    }

    @Override
    protected void validarEliminar(Long id, Rol rol) {
        // Regla: no se puede eliminar un rol que todavia tiene usuarios asignados
        if (usuarioRepository.existsByRol_IdRol(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar: el rol tiene usuarios asignados");
        }
    }

    // El controller solo expone listar y eliminar; crear y actualizar se implementan por el contrato genérico
    @Override
    protected Rol toEntity(RolRequestDTO dto) {
        return Rol.builder().nombre(dto.getNombre().trim()).build();
    }

    @Override
    protected void actualizarEntidad(Rol rol, RolRequestDTO dto) {
        rol.setNombre(dto.getNombre().trim());
    }

    @Override
    protected RolResponseDTO toResponse(Rol r) {
        return RolResponseDTO.builder()
                .idRol(r.getIdRol())
                .nombre(r.getNombre())
                .build();
    }
}
