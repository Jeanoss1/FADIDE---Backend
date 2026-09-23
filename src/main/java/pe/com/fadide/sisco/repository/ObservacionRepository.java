package pe.com.fadide.sisco.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pe.com.fadide.sisco.model.Observacion;

// Patrón Repository: acceso a datos abstraído con Spring Data JPA
@Repository
public interface ObservacionRepository extends JpaRepository<Observacion, Long> {

    @Query("select o from Observacion o order by o.fecha desc, o.idObservacion desc")
    List<Observacion> listarTodas();

    @Query("select o from Observacion o where o.supervision.idSupervision = :idSupervision order by o.fecha desc, o.idObservacion desc")
    List<Observacion> buscarPorSupervision(@Param("idSupervision") Long idSupervision);

    @Query("select count(o) > 0 from Observacion o where o.supervision.idSupervision = :idSupervision")
    boolean existsBySupervisionId(@Param("idSupervision") Long idSupervision);
}
