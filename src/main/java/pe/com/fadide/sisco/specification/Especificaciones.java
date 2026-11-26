package pe.com.fadide.sisco.specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import pe.com.fadide.sisco.config.BadRequestException;
import pe.com.fadide.sisco.dto.FiltroBusqueda;
import pe.com.fadide.sisco.model.Cliente;
import pe.com.fadide.sisco.model.Contratista;
import pe.com.fadide.sisco.model.Contrato;
import pe.com.fadide.sisco.model.EstadoProyecto;
import pe.com.fadide.sisco.model.Licencia;
import pe.com.fadide.sisco.model.Pago;
import pe.com.fadide.sisco.model.Penalidad;
import pe.com.fadide.sisco.model.Proyecto;
import pe.com.fadide.sisco.model.Supervision;
import pe.com.fadide.sisco.model.Usuario;

/**
 * Patrón Specification: cada método arma, a partir de FiltroBusqueda, los criterios WHERE
 * de un recurso con la API Criteria de JPA. Solo se agregan los filtros que llegan con valor,
 * de modo que un mismo endpoint sirve para listar todo o para combinar búsqueda, estado y
 * rango de fechas. Las mismas especificaciones las reutilizan los listados paginados y los
 * reportes exportables, y como los valores viajan como parámetros (nunca concatenados en el
 * SQL) quedan protegidas contra inyección SQL.
 */
public final class Especificaciones {

    private Especificaciones() {
    }

    public static Specification<Cliente> clientes(FiltroBusqueda f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (f.tieneTexto()) {
                p.add(cb.or(contiene(cb, root.get("razonSocial"), f), contiene(cb, root.get("ruc"), f)));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }

    public static Specification<Contratista> contratistas(FiltroBusqueda f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (f.tieneTexto()) {
                p.add(cb.or(contiene(cb, root.get("nombre"), f), contiene(cb, root.get("ruc"), f)));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }

    public static Specification<Proyecto> proyectos(FiltroBusqueda f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (f.tieneTexto()) {
                p.add(cb.or(
                        contiene(cb, root.get("nombre"), f),
                        contiene(cb, root.get("ubicacion"), f),
                        contiene(cb, root.get("cliente").get("razonSocial"), f)));
            }
            if (f.tieneEstado()) {
                p.add(cb.equal(root.get("estado"), estadoProyecto(f.estado())));
            }
            if (f.idCliente() != null) {
                p.add(cb.equal(root.get("cliente").get("idCliente"), f.idCliente()));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }

    public static Specification<Licencia> licencias(FiltroBusqueda f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (f.tieneTexto()) {
                p.add(cb.or(contiene(cb, root.get("numero"), f), contiene(cb, root.get("proyecto").get("nombre"), f)));
            }
            rangoFechas(cb, root.get("fechaVencimiento"), f, p);
            if (f.idProyecto() != null) {
                p.add(cb.equal(root.get("proyecto").get("idProyecto"), f.idProyecto()));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }

    public static Specification<Contrato> contratos(FiltroBusqueda f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (f.tieneTexto()) {
                p.add(cb.or(contiene(cb, root.get("numeroContrato"), f), contiene(cb, root.get("proyecto").get("nombre"), f)));
            }
            if (f.tieneEstado()) {
                p.add(cb.equal(cb.upper(root.get("estado")), f.estado().trim().toUpperCase()));
            }
            rangoFechas(cb, root.get("fechaInicio"), f, p);
            if (f.idProyecto() != null) {
                p.add(cb.equal(root.get("proyecto").get("idProyecto"), f.idProyecto()));
            }
            if (f.idCliente() != null) {
                p.add(cb.equal(root.get("proyecto").get("cliente").get("idCliente"), f.idCliente()));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }

    public static Specification<Pago> pagos(FiltroBusqueda f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (f.tieneTexto()) {
                p.add(cb.or(
                        contiene(cb, root.get("contrato").get("numeroContrato"), f),
                        contiene(cb, root.get("contrato").get("proyecto").get("nombre"), f)));
            }
            if (f.tieneEstado()) {
                p.add(cb.equal(cb.upper(root.get("estado")), f.estado().trim().toUpperCase()));
            }
            rangoFechas(cb, root.get("fechaPago"), f, p);
            if (f.idContrato() != null) {
                p.add(cb.equal(root.get("contrato").get("idContrato"), f.idContrato()));
            }
            if (f.idProyecto() != null) {
                p.add(cb.equal(root.get("contrato").get("proyecto").get("idProyecto"), f.idProyecto()));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }

    public static Specification<Penalidad> penalidades(FiltroBusqueda f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (f.tieneTexto()) {
                p.add(cb.or(contiene(cb, root.get("motivo"), f), contiene(cb, root.get("contrato").get("numeroContrato"), f)));
            }
            if (f.tieneEstado()) {
                p.add(cb.equal(cb.upper(root.get("estado")), f.estado().trim().toUpperCase()));
            }
            rangoFechas(cb, root.get("fechaAplicacion"), f, p);
            if (f.idContrato() != null) {
                p.add(cb.equal(root.get("contrato").get("idContrato"), f.idContrato()));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }

    public static Specification<Supervision> supervisiones(FiltroBusqueda f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (f.tieneTexto()) {
                p.add(cb.or(
                        contiene(cb, root.get("contrato").get("numeroContrato"), f),
                        contiene(cb, root.get("contrato").get("proyecto").get("nombre"), f),
                        contiene(cb, root.get("observaciones"), f)));
            }
            rangoFechas(cb, root.get("fechaRegistro"), f, p);
            if (f.idUsuario() != null) {
                p.add(cb.equal(root.get("usuario").get("idUsuario"), f.idUsuario()));
            }
            if (f.idProyecto() != null) {
                p.add(cb.equal(root.get("contrato").get("proyecto").get("idProyecto"), f.idProyecto()));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }

    public static Specification<Usuario> usuarios(FiltroBusqueda f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (f.tieneTexto()) {
                p.add(cb.or(
                        contiene(cb, root.get("nombreCompleto"), f),
                        contiene(cb, root.get("correo"), f),
                        contiene(cb, root.get("documento"), f)));
            }
            if (f.idRol() != null) {
                p.add(cb.equal(root.get("rol").get("idRol"), f.idRol()));
            }
            if (f.tieneEstado()) {
                // estado = ACTIVO / INACTIVO
                p.add(cb.equal(root.get("activo"), "ACTIVO".equalsIgnoreCase(f.estado().trim())));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }

    // ---------- utilitarios ----------

    private static Predicate contiene(CriteriaBuilder cb, Expression<String> campo, FiltroBusqueda f) {
        return cb.like(cb.lower(campo), f.patron());
    }

    private static void rangoFechas(CriteriaBuilder cb, Path<LocalDate> campo, FiltroBusqueda f, List<Predicate> p) {
        if (f.desde() != null && f.hasta() != null && f.hasta().isBefore(f.desde())) {
            throw new BadRequestException("La fecha 'hasta' no puede ser anterior a la fecha 'desde'");
        }
        if (f.desde() != null) {
            p.add(cb.greaterThanOrEqualTo(campo, f.desde()));
        }
        if (f.hasta() != null) {
            p.add(cb.lessThanOrEqualTo(campo, f.hasta()));
        }
    }

    private static EstadoProyecto estadoProyecto(String valor) {
        try {
            return EstadoProyecto.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Estado de obra no válido: " + valor);
        }
    }
}
