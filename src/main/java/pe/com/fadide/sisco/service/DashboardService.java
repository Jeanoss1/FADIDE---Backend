package pe.com.fadide.sisco.service;

import pe.com.fadide.sisco.dto.DashboardResponse;

public interface DashboardService {

    /**
     * Indicadores y gráficos del panel principal según el rol del usuario.
     *
     * @param correo correo del usuario autenticado (nombre del principal en el JWT)
     */
    DashboardResponse obtener(String correo);
}
