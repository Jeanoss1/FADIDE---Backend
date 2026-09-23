package pe.com.fadide.sisco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.fadide.sisco.model.Contrato;

public interface ContratoRepository extends JpaRepository<Contrato, Long> { }