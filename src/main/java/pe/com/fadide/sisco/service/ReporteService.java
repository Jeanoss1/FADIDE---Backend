package pe.com.fadide.sisco.service;

import java.util.List;

import pe.com.fadide.sisco.dto.FiltroBusqueda;
import pe.com.fadide.sisco.dto.OpcionResponse;
import pe.com.fadide.sisco.dto.TablaReporte;

public interface ReporteService {

    /**
     * Genera un reporte con los filtros indicados.
     *
     * @param tipo contratos, pagos, penalidades, supervisiones, licencias u obras
     */
    TablaReporte generar(String tipo, FiltroBusqueda filtro);

    // Supervisores para el filtro "usuario" del reporte de supervisiones
    List<OpcionResponse> supervisores();
}
