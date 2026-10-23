package pe.com.fadide.sisco.service;

import java.util.List;

import pe.com.fadide.sisco.common.service.CrudService;
import pe.com.fadide.sisco.dto.ProyectoRequest;
import pe.com.fadide.sisco.dto.ProyectoResponse;
import pe.com.fadide.sisco.model.EstadoProyecto;

public interface ProyectoService extends CrudService<ProyectoRequest, ProyectoResponse, Long> {
    List<ProyectoResponse> listar(EstadoProyecto estado, Long idCliente);
}
