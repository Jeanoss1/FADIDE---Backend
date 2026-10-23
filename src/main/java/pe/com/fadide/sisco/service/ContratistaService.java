package pe.com.fadide.sisco.service;

import java.util.List;

import pe.com.fadide.sisco.common.service.CrudService;
import pe.com.fadide.sisco.dto.ContratistaRequest;
import pe.com.fadide.sisco.dto.ContratistaResponse;

public interface ContratistaService extends CrudService<ContratistaRequest, ContratistaResponse, Long> {
    List<ContratistaResponse> listar(String nombre);
}
