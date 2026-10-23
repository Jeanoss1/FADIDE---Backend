package pe.com.fadide.sisco.service;

import pe.com.fadide.sisco.common.service.CrudService;
import pe.com.fadide.sisco.dto.SupervisionRequest;
import pe.com.fadide.sisco.dto.SupervisionResponse;

public interface SupervisionService extends CrudService<SupervisionRequest, SupervisionResponse, Long> {
    SupervisionResponse obtenerPorContrato(Long idContrato);
}
