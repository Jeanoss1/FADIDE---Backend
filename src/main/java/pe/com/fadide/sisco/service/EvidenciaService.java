package pe.com.fadide.sisco.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.com.fadide.sisco.config.ResourceNotFoundException;
import pe.com.fadide.sisco.dto.EvidenciaRequest;
import pe.com.fadide.sisco.dto.EvidenciaResponse;
import pe.com.fadide.sisco.model.Evidencia;
import pe.com.fadide.sisco.model.Supervision;
import pe.com.fadide.sisco.repository.EvidenciaRepository;
import pe.com.fadide.sisco.repository.SupervisionRepository;

// Patrón Singleton: bean de Spring con una única instancia
@Service
public class EvidenciaService {

    private final EvidenciaRepository evidenciaRepository;
    private final SupervisionRepository supervisionRepository;

    public EvidenciaService(EvidenciaRepository evidenciaRepository,
                            SupervisionRepository supervisionRepository) {
        this.evidenciaRepository = evidenciaRepository;
        this.supervisionRepository = supervisionRepository;
    }

    @Transactional(readOnly = true)
    public List<EvidenciaResponse> listar(Long idSupervision) {
        List<Evidencia> evidencias = idSupervision == null
                ? evidenciaRepository.listarTodas()
                : evidenciaRepository.buscarPorSupervision(idSupervision);
        return evidencias.stream().map(EvidenciaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public EvidenciaResponse obtener(Long id) {
        return EvidenciaResponse.from(buscar(id));
    }

    @Transactional
    public EvidenciaResponse crear(EvidenciaRequest request) {
        Evidencia evidencia = Evidencia.builder()
                .imagen(request.imagen().trim())
                .descripcion(request.descripcion())
                .fecha(request.fecha())
                .supervision(buscarSupervision(request.idSupervision()))
                .build();
        return EvidenciaResponse.from(evidenciaRepository.save(evidencia));
    }

    @Transactional
    public EvidenciaResponse actualizar(Long id, EvidenciaRequest request) {
        Evidencia evidencia = buscar(id);
        evidencia.setImagen(request.imagen().trim());
        evidencia.setDescripcion(request.descripcion());
        evidencia.setFecha(request.fecha());
        evidencia.setSupervision(buscarSupervision(request.idSupervision()));
        return EvidenciaResponse.from(evidenciaRepository.save(evidencia));
    }

    @Transactional
    public void eliminar(Long id) {
        evidenciaRepository.delete(buscar(id));
    }

    private Evidencia buscar(Long id) {
        return evidenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidencia no encontrada con id " + id));
    }

    private Supervision buscarSupervision(Long idSupervision) {
        return supervisionRepository.findById(idSupervision)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supervisión no encontrada con id " + idSupervision));
    }
}
