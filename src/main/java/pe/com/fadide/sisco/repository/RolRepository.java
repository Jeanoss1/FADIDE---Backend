package pe.com.fadide.sisco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.fadide.sisco.model.Rol;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(String nombre);
}
