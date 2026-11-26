package pe.com.fadide.sisco.dto;

import java.util.List;

/**
 * Reporte ya calculado y con los valores formateados. Es el mismo objeto que se devuelve como
 * JSON para la vista previa del frontend y el que reciben los exportadores PDF y Excel, de modo
 * que lo que el usuario ve en pantalla coincide con lo que descarga.
 *
 * @param titulo   título del reporte (por ejemplo, "Reporte de pagos")
 * @param filtros  descripción legible de los filtros aplicados
 * @param columnas encabezados de la tabla
 * @param numericas índices de las columnas numéricas (se alinean a la derecha)
 * @param filas    valores de cada fila, en el mismo orden que las columnas
 * @param totales  fila de totales (misma cantidad de celdas que columnas) o null si no aplica
 */
public record TablaReporte(
        String titulo,
        String filtros,
        List<String> columnas,
        List<Integer> numericas,
        List<List<String>> filas,
        List<String> totales
) {
}
