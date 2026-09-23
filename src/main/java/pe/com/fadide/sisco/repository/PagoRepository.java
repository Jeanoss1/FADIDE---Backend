package pe.com.fadide.sisco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.fadide.sisco.model.Pago;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByContrato_IdContrato(Long idContrato);

    boolean existsByContrato_IdContrato(Long idContrato);
}