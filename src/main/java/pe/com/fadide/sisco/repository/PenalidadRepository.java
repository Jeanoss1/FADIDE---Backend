package pe.com.fadide.sisco.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import pe.com.fadide.sisco.model.Penalidad;

public interface PenalidadRepository extends JpaRepository<Penalidad, Long>, JpaSpecificationExecutor<Penalidad> {

    boolean existsByContrato_IdContrato(Long idContrato);

    @Query("select coalesce(sum(p.montoUit), 0) from Penalidad p where upper(p.estado) = 'APLICADA'")
    BigDecimal sumarUitAplicadas();
}
