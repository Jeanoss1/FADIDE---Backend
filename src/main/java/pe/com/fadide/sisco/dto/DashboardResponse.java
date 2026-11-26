package pe.com.fadide.sisco.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Panel principal (GET /api/dashboard). El backend decide qué indicadores y gráficos
 * corresponden al rol del usuario autenticado; el frontend solo los dibuja.
 */
public record DashboardResponse(
        String rol,
        List<Kpi> kpis,
        List<Grafico> graficos,
        List<LicenciaResponse> licenciasPorVencer
) {

    /**
     * Indicador clave.
     *
     * @param formato NUMERO, MONEDA (soles), PORCENTAJE o UIT; el frontend lo usa para darle formato
     * @param icono   nombre del ícono de Material Icons
     * @param ruta    pantalla del frontend que abre el indicador (puede ser null)
     */
    public record Kpi(String clave, String etiqueta, BigDecimal valor, String formato, String icono, String ruta) {
    }

    /**
     * Gráfico listo para dibujar.
     *
     * @param tipo    COLUMNAS (serie en el tiempo), BARRAS (partes ordenadas de mayor a menor)
     *                o MEDIDORES (porcentaje de cada fila frente a 100 %)
     * @param formato formato de los valores (NUMERO, MONEDA, PORCENTAJE)
     */
    public record Grafico(String clave, String titulo, String tipo, String formato, List<Punto> datos) {
    }

    public record Punto(String etiqueta, BigDecimal valor) {
    }
}
