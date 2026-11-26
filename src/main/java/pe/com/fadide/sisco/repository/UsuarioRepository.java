package pe.com.fadide.sisco.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import pe.com.fadide.sisco.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    boolean existsByRol_IdRol(Long idRol);

    boolean existsByDocumentoAndRol_Nombre(String documento, String nombreRol);

    long countByActivoTrue();

    List<Usuario> findByRol_NombreOrderByNombreCompleto(String nombreRol);
}
