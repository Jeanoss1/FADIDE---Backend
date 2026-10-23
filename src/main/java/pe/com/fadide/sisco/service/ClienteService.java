package pe.com.fadide.sisco.service;

import java.util.List;

import pe.com.fadide.sisco.common.service.CrudService;
import pe.com.fadide.sisco.dto.ClienteRequest;
import pe.com.fadide.sisco.dto.ClienteResponse;

public interface ClienteService extends CrudService<ClienteRequest, ClienteResponse, Long> {
    List<ClienteResponse> listar(String razonSocial);
}
