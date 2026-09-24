package pe.com.fadide.sisco.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pe.com.fadide.sisco.model.Supervision;

// Patrón Repository: acceso a datos abstraído con Spring Data JPA
@Repository
public interface SupervisionRepository extends JpaRepository<Supervision, Long> {

    @Query("select s from Supervision s order by s.fechaRegistro desc")
    List<Supervision> listarTodas();

    @Query("select count(s) > 0 from Supervision s where s.contrato.idContrato = :idContrato")
    boolean existsByContratoId(@Param("idContrato") Long idContrato);

    @Query("select count(s) > 0 from Supervision s where s.contrato.idContrato = :idContrato and s.idSupervision <> :idSupervision")
    boolean existsByContratoIdAndIdSupervisionNot(@Param("idContrato") Long idContrato,
                                                  @Param("idSupervision") Long idSupervision);

    @Query("select s from Supervision s where s.contrato.idContrato = :idContrato")
    Optional<Supervision> buscarPorContrato(@Param("idContrato") Long idContrato);
}
