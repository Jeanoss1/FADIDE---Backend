package pe.com.fadide.sisco.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pe.com.fadide.sisco.model.EstadoProyecto;
import pe.com.fadide.sisco.model.Proyecto;

// Patrón Repository: acceso a datos abstraído con Spring Data JPA
@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {

    @Query("select p from Proyecto p order by p.nombre")
    List<Proyecto> listarTodos();

    @Query("select p from Proyecto p where p.estado = :estado order by p.nombre")
    List<Proyecto> buscarPorEstado(@Param("estado") EstadoProyecto estado);

    @Query("select p from Proyecto p where p.cliente.idCliente = :idCliente order by p.nombre")
    List<Proyecto> buscarPorCliente(@Param("idCliente") Long idCliente);

    @Query("select p from Proyecto p where p.estado = :estado and p.cliente.idCliente = :idCliente order by p.nombre")
    List<Proyecto> buscarPorEstadoYCliente(@Param("estado") EstadoProyecto estado,
                                           @Param("idCliente") Long idCliente);
}
