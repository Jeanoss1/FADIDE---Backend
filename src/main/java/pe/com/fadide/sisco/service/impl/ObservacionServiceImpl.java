package pe.com.fadide.sisco.service.impl;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.com.fadide.sisco.common.exception.RecursoNoEncontradoException;
import pe.com.fadide.sisco.common.service.impl.CrudServiceImpl;
import pe.com.fadide.sisco.dto.ObservacionRequest;
import pe.com.fadide.sisco.dto.ObservacionResponse;
import pe.com.fadide.sisco.model.Observacion;
import pe.com.fadide.sisco.model.Supervision;
import pe.com.fadide.sisco.repository.ObservacionRepository;
import pe.com.fadide.sisco.repository.SupervisionRepository;
import pe.com.fadide.sisco.service.ObservacionService;

// Patrón Singleton: bean de Spring con una única instancia
@Service
@RequiredArgsConstructor
public class ObservacionServiceImpl
        extends CrudServiceImpl<Observacion, ObservacionRequest, ObservacionResponse, Long>
        implements ObservacionService {

    private final ObservacionRepository observacionRepository;
    private final SupervisionRepository supervisionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ObservacionResponse> listar(Long idSupervision) {
        if (idSupervision == null) {
            return listar();
        }
        return observacionRepository.buscarPorSupervision(idSupervision).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    protected JpaRepository<Observacion, Long> getRepository() {
        return observacionRepository;
    }

    @Override
    protected List<Observacion> buscarTodos() {
        return observacionRepository.listarTodas();
    }

    @Override
    protected String mensajeNoEncontrado(Long id) {
        return "Observación no encontrada con id " + id;
    }

    @Override
    protected Observacion toEntity(ObservacionRequest request) {
        return Observacion.builder()
                .descripcion(request.descripcion().trim())
                .fecha(request.fecha())
                .supervision(buscarSupervision(request.idSupervision()))
                .build();
    }

    @Override
    protected void actualizarEntidad(Observacion observacion, ObservacionRequest request) {
        observacion.setDescripcion(request.descripcion().trim());
        observacion.setFecha(request.fecha());
        observacion.setSupervision(buscarSupervision(request.idSupervision()));
    }

    @Override
    protected ObservacionResponse toResponse(Observacion observacion) {
        return ObservacionResponse.from(observacion);
    }

    private Supervision buscarSupervision(Long idSupervision) {
        return supervisionRepository.findById(idSupervision)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Supervisión no encontrada con id " + idSupervision));
    }
}
