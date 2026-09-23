package pe.com.fadide.sisco.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pe.com.fadide.sisco.model.Licencia;

// Patrón Repository: acceso a datos abstraído con Spring Data JPA
@Repository
public interface LicenciaRepository extends JpaRepository<Licencia, Long> {

    boolean existsByNumero(String numero);

    boolean existsByNumeroAndIdLicenciaNot(String numero, Long idLicencia);

    @Query("select count(l) > 0 from Licencia l where l.proyecto.idProyecto = :idProyecto")
    boolean existsByProyectoId(@Param("idProyecto") Long idProyecto);

    @Query("select count(l) > 0 from Licencia l where l.proyecto.idProyecto = :idProyecto and l.idLicencia <> :idLicencia")
    boolean existsByProyectoIdAndIdLicenciaNot(@Param("idProyecto") Long idProyecto,
                                               @Param("idLicencia") Long idLicencia);

    @Query("select l from Licencia l where l.proyecto.idProyecto = :idProyecto")
    Optional<Licencia> buscarPorProyecto(@Param("idProyecto") Long idProyecto);

    @Query("select l from Licencia l where l.fechaVencimiento between :desde and :hasta order by l.fechaVencimiento")
    List<Licencia> buscarPorVencer(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}
