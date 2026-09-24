package pe.com.fadide.sisco.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pe.com.fadide.sisco.model.Contratista;

// Patrón Repository: acceso a datos abstraído con Spring Data JPA
@Repository
public interface ContratistaRepository extends JpaRepository<Contratista, Long> {

    boolean existsByRuc(String ruc);

    boolean existsByRucAndIdContratistaNot(String ruc, Long idContratista);

    @Query("select c from Contratista c where lower(c.razonSocial) like lower(concat('%', :texto, '%')) order by c.razonSocial")
    List<Contratista> buscarPorNombre(@Param("texto") String texto);
}
