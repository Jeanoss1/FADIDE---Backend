package pe.com.fadide.sisco.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.com.fadide.sisco.config.ResourceNotFoundException;
import pe.com.fadide.sisco.dto.ObservacionRequest;
import pe.com.fadide.sisco.dto.ObservacionResponse;
import pe.com.fadide.sisco.model.Observacion;
import pe.com.fadide.sisco.model.Supervision;
import pe.com.fadide.sisco.repository.ObservacionRepository;
import pe.com.fadide.sisco.repository.SupervisionRepository;

// Patrón Singleton: bean de Spring con una única instancia
@Service
public class ObservacionService {

    private final ObservacionRepository observacionRepository;
    private final SupervisionRepository supervisionRepository;

    public ObservacionService(ObservacionRepository observacionRepository,
                              SupervisionRepository supervisionRepository) {
        this.observacionRepository = observacionRepository;
        this.supervisionRepository = supervisionRepository;
    }

    @Transactional(readOnly = true)
    public List<ObservacionResponse> listar(Long idSupervision) {
        List<Observacion> observaciones = idSupervision == null
                ? observacionRepository.listarTodas()
                : observacionRepository.buscarPorSupervision(idSupervision);
        return observaciones.stream().map(ObservacionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ObservacionResponse obtener(Long id) {
        return ObservacionResponse.from(buscar(id));
    }

    @Transactional
    public ObservacionResponse crear(ObservacionRequest request) {
        Observacion observacion = Observacion.builder()
                .descripcion(request.descripcion().trim())
                .fecha(request.fecha())
                .supervision(buscarSupervision(request.idSupervision()))
                .build();
        return ObservacionResponse.from(observacionRepository.save(observacion));
    }

    @Transactional
    public ObservacionResponse actualizar(Long id, ObservacionRequest request) {
        Observacion observacion = buscar(id);
        observacion.setDescripcion(request.descripcion().trim());
        observacion.setFecha(request.fecha());
        observacion.setSupervision(buscarSupervision(request.idSupervision()));
        return ObservacionResponse.from(observacionRepository.save(observacion));
    }

    @Transactional
    public void eliminar(Long id) {
        observacionRepository.delete(buscar(id));
    }

    private Observacion buscar(Long id) {
        return observacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Observación no encontrada con id " + id));
    }

    private Supervision buscarSupervision(Long idSupervision) {
        return supervisionRepository.findById(idSupervision)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supervisión no encontrada con id " + idSupervision));
    }
}
