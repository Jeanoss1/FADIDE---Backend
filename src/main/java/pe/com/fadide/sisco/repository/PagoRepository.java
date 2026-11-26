package pe.com.fadide.sisco.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.com.fadide.sisco.model.Pago;

public interface PagoRepository extends JpaRepository<Pago, Long>, JpaSpecificationExecutor<Pago> {

    List<Pago> findByContrato_IdContrato(Long idContrato);

    boolean existsByContrato_IdContrato(Long idContrato);

    // Monto pagado sin contar los pagos anulados
    @Query("select coalesce(sum(p.monto), 0) from Pago p where upper(p.estado) <> 'ANULADO'")
    BigDecimal sumarPagado();

    // Total pagado por contrato -> [idContrato, total]; lo usan el dashboard y los reportes
    @Query("select p.contrato.idContrato, coalesce(sum(p.monto), 0) from Pago p where upper(p.estado) <> 'ANULADO' group by p.contrato.idContrato")
    List<Object[]> sumarPagadoPorContrato();

    @Query("select p from Pago p where p.fechaPago >= :desde and upper(p.estado) <> 'ANULADO'")
    List<Pago> buscarDesde(@Param("desde") LocalDate desde);
}
