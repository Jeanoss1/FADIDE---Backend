package pe.com.fadide.sisco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.fadide.sisco.model.Proyecto;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {
}