package pe.com.fadide.sisco.service;

import java.util.List;

import pe.com.fadide.sisco.common.service.CrudService;
import pe.com.fadide.sisco.dto.ObservacionRequest;
import pe.com.fadide.sisco.dto.ObservacionResponse;

public interface ObservacionService extends CrudService<ObservacionRequest, ObservacionResponse, Long> {
    List<ObservacionResponse> listar(Long idSupervision);
}
