package pe.com.fadide.sisco.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * Filtros comunes de los listados paginados y de los reportes. Todos son opcionales; cada
 * recurso usa solo los que le aplican (ver Especificaciones).
 *
 * @param q          texto libre (nombre, RUC, número de contrato, etc.)
 * @param estado     estado del registro (VIGENTE, REGISTRADO, EN_EJECUCION...)
 * @param desde      fecha inicial del rango (inclusive), formato yyyy-MM-dd
 * @param hasta      fecha final del rango (inclusive), formato yyyy-MM-dd
 * @param idProyecto obra a la que pertenece el registro
 * @param idContrato contrato al que pertenece el registro
 * @param idCliente  cliente dueño de la obra
 * @param idUsuario  usuario que registró la supervisión
 * @param idRol      rol del usuario (listado de usuarios)
 */
public record FiltroBusqueda(
        String q,
        String estado,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
        Long idProyecto,
        Long idContrato,
        Long idCliente,
        Long idUsuario,
        Long idRol
) {
    public boolean tieneTexto() {
        return q != null && !q.isBlank();
    }

    public boolean tieneEstado() {
        return estado != null && !estado.isBlank();
    }

    // Patrón para LIKE sin distinguir mayúsculas: "%texto%"
    public String patron() {
        return "%" + q.trim().toLowerCase() + "%";
    }
}
