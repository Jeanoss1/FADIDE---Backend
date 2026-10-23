package pe.com.fadide.sisco.service;

import java.util.List;

import pe.com.fadide.sisco.common.service.CrudService;
import pe.com.fadide.sisco.dto.LicenciaRequest;
import pe.com.fadide.sisco.dto.LicenciaResponse;

public interface LicenciaService extends CrudService<LicenciaRequest, LicenciaResponse, Long> {
    LicenciaResponse obtenerPorProyecto(Long idProyecto);
    List<LicenciaResponse> listarPorVencer(int dias);
}
