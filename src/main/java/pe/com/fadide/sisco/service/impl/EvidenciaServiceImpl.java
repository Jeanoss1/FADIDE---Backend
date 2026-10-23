package pe.com.fadide.sisco.service.impl;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.com.fadide.sisco.common.exception.RecursoNoEncontradoException;
import pe.com.fadide.sisco.common.service.impl.CrudServiceImpl;
import pe.com.fadide.sisco.dto.EvidenciaRequest;
import pe.com.fadide.sisco.dto.EvidenciaResponse;
import pe.com.fadide.sisco.model.Evidencia;
import pe.com.fadide.sisco.model.Supervision;
import pe.com.fadide.sisco.repository.EvidenciaRepository;
import pe.com.fadide.sisco.repository.SupervisionRepository;
import pe.com.fadide.sisco.service.EvidenciaService;

// Patrón Singleton: bean de Spring con una única instancia
@Service
@RequiredArgsConstructor
public class EvidenciaServiceImpl extends CrudServiceImpl<Evidencia, EvidenciaRequest, EvidenciaResponse, Long>
        implements EvidenciaService {

    private final EvidenciaRepository evidenciaRepository;
    private final SupervisionRepository supervisionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EvidenciaResponse> listar(Long idSupervision) {
        if (idSupervision == null) {
            return listar();
        }
        return evidenciaRepository.buscarPorSupervision(idSupervision).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    protected JpaRepository<Evidencia, Long> getRepository() {
        return evidenciaRepository;
    }

    @Override
    protected List<Evidencia> buscarTodos() {
        return evidenciaRepository.listarTodas();
    }

    @Override
    protected String mensajeNoEncontrado(Long id) {
        return "Evidencia no encontrada con id " + id;
    }

    @Override
    protected Evidencia toEntity(EvidenciaRequest request) {
        return Evidencia.builder()
                .imagen(request.imagen().trim())
                .descripcion(request.descripcion())
                .fecha(request.fecha())
                .supervision(buscarSupervision(request.idSupervision()))
                .build();
    }

    @Override
    protected void actualizarEntidad(Evidencia evidencia, EvidenciaRequest request) {
        evidencia.setImagen(request.imagen().trim());
        evidencia.setDescripcion(request.descripcion());
        evidencia.setFecha(request.fecha());
        evidencia.setSupervision(buscarSupervision(request.idSupervision()));
    }

    @Override
    protected EvidenciaResponse toResponse(Evidencia evidencia) {
        return EvidenciaResponse.from(evidencia);
    }

    private Supervision buscarSupervision(Long idSupervision) {
        return supervisionRepository.findById(idSupervision)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Supervisión no encontrada con id " + idSupervision));
    }
}
