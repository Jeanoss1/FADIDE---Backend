package pe.com.fadide.sisco.service;

import java.util.List;

import pe.com.fadide.sisco.common.service.CrudService;
import pe.com.fadide.sisco.dto.EvidenciaRequest;
import pe.com.fadide.sisco.dto.EvidenciaResponse;

public interface EvidenciaService extends CrudService<EvidenciaRequest, EvidenciaResponse, Long> {
    List<EvidenciaResponse> listar(Long idSupervision);
}
