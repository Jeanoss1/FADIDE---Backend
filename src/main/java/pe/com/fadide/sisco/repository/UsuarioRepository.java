package pe.com.fadide.sisco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.fadide.sisco.model.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    boolean existsByRol_IdRol(Long idRol);
}
