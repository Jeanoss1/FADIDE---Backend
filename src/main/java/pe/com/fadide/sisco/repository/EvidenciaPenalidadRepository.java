package pe.com.fadide.sisco.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.com.fadide.sisco.model.EvidenciaPenalidad;

public interface EvidenciaPenalidadRepository extends JpaRepository<EvidenciaPenalidad, Long> {

    List<EvidenciaPenalidad> findByPenalidad_IdPenalidadOrderByFechaDescIdEvidenciaPenalidadDesc(Long idPenalidad);

    long countByPenalidad_IdPenalidad(Long idPenalidad);
}
