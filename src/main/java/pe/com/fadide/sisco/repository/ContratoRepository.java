package pe.com.fadide.sisco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.fadide.sisco.model.Contrato;

import java.util.List;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    @Query("select c from Contrato c where c.proyecto.idProyecto = :idProyecto order by c.fechaInicio desc")
    List<Contrato> buscarPorProyecto(@Param("idProyecto") Long idProyecto);

    @Query("select count(c) > 0 from Contrato c where c.proyecto.idProyecto = :idProyecto")
    boolean existsByProyectoId(@Param("idProyecto") Long idProyecto);

    boolean existsByNumeroContrato(String numeroContrato);
}
