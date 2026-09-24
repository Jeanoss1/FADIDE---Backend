package pe.com.fadide.sisco.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pe.com.fadide.sisco.model.Evidencia;

// Patrón Repository: acceso a datos abstraído con Spring Data JPA
@Repository
public interface EvidenciaRepository extends JpaRepository<Evidencia, Long> {

    @Query("select e from Evidencia e order by e.fecha desc, e.idEvidencia desc")
    List<Evidencia> listarTodas();

    @Query("select e from Evidencia e where e.supervision.idSupervision = :idSupervision order by e.fecha desc, e.idEvidencia desc")
    List<Evidencia> buscarPorSupervision(@Param("idSupervision") Long idSupervision);

    @Query("select count(e) > 0 from Evidencia e where e.supervision.idSupervision = :idSupervision")
    boolean existsBySupervisionId(@Param("idSupervision") Long idSupervision);
}
