package pe.com.fadide.sisco.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pe.com.fadide.sisco.model.Cliente;

// Patrón Repository: acceso a datos abstraído con Spring Data JPA
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByNumeroDocumento(String numeroDocumento);

    boolean existsByNumeroDocumentoAndIdClienteNot(String numeroDocumento, Long idCliente);

    @Query("select c from Cliente c where lower(c.nombreORazonSocial) like lower(concat('%', :texto, '%')) order by c.nombreORazonSocial")
    List<Cliente> buscarPorRazonSocial(@Param("texto") String texto);
}
