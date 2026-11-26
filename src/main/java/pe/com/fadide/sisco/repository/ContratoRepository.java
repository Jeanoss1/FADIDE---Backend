package pe.com.fadide.sisco.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.com.fadide.sisco.model.Contrato;

public interface ContratoRepository extends JpaRepository<Contrato, Long>, JpaSpecificationExecutor<Contrato> {

    @Query("select c from Contrato c where c.proyecto.idProyecto = :idProyecto order by c.fechaInicio desc")
    List<Contrato> buscarPorProyecto(@Param("idProyecto") Long idProyecto);

    @Query("select count(c) > 0 from Contrato c where c.proyecto.idProyecto = :idProyecto")
    boolean existsByProyectoId(@Param("idProyecto") Long idProyecto);

    boolean existsByNumeroContrato(String numeroContrato);

    // Dashboard: contratos que aún no se cierran (ni CONFORME ni RESUELTO)
    @Query("select count(c) from Contrato c where upper(c.estado) not in ('CONFORME', 'RESUELTO')")
    long contarAbiertos();

    @Query("select coalesce(sum(c.montoTotal), 0) from Contrato c")
    BigDecimal sumarMontoTotal();

    // Dashboard: cantidad de contratos agrupados por estado -> [estado, total]
    @Query("select c.estado, count(c) from Contrato c group by c.estado")
    List<Object[]> contarPorEstado();
}
