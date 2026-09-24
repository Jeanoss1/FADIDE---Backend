package pe.com.fadide.sisco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.fadide.sisco.model.Contrato;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    @Query("select count(c) > 0 from Contrato c where c.proyecto.idProyecto = :idProyecto")
    boolean existsByProyectoId(@Param("idProyecto") Long idProyecto);
}
