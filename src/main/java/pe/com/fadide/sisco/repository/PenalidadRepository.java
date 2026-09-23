package pe.com.fadide.sisco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.fadide.sisco.model.Penalidad;

public interface PenalidadRepository extends JpaRepository<Penalidad, Long> {

    boolean existsByContrato_IdContrato(Long idContrato);
}