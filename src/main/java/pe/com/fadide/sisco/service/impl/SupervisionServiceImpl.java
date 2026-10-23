package pe.com.fadide.sisco.service.impl;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.com.fadide.sisco.common.exception.RecursoNoEncontradoException;
import pe.com.fadide.sisco.common.service.impl.CrudServiceImpl;
import pe.com.fadide.sisco.config.ConflictException;
import pe.com.fadide.sisco.dto.SupervisionRequest;
import pe.com.fadide.sisco.dto.SupervisionResponse;
import pe.com.fadide.sisco.model.Contrato;
import pe.com.fadide.sisco.model.Supervision;
import pe.com.fadide.sisco.model.Usuario;
import pe.com.fadide.sisco.repository.ContratoRepository;
import pe.com.fadide.sisco.repository.EvidenciaRepository;
import pe.com.fadide.sisco.repository.ObservacionRepository;
import pe.com.fadide.sisco.repository.SupervisionRepository;
import pe.com.fadide.sisco.repository.UsuarioRepository;
import pe.com.fadide.sisco.service.SupervisionService;

// Patrón Singleton: bean de Spring con una única instancia
@Service
@RequiredArgsConstructor
public class SupervisionServiceImpl
        extends CrudServiceImpl<Supervision, SupervisionRequest, SupervisionResponse, Long>
        implements SupervisionService {

    private final SupervisionRepository supervisionRepository;
    private final ContratoRepository contratoRepository;
    private final ObservacionRepository observacionRepository;
    private final EvidenciaRepository evidenciaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public SupervisionResponse obtenerPorContrato(Long idContrato) {
        return supervisionRepository.buscarPorContrato(idContrato)
                .map(this::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una supervisión para el contrato con id " + idContrato));
    }

    @Override
    protected JpaRepository<Supervision, Long> getRepository() {
        return supervisionRepository;
    }

    @Override
    protected List<Supervision> buscarTodos() {
        return supervisionRepository.listarTodas();
    }

    @Override
    protected String mensajeNoEncontrado(Long id) {
        return "Supervisión no encontrada con id " + id;
    }

    @Override
    protected void validarCrear(SupervisionRequest request) {
        if (supervisionRepository.existsByContratoId(request.idContrato())) {
            throw new ConflictException("El contrato ya tiene una supervisión asociada");
        }
    }

    @Override
    protected void validarActualizar(Long id, SupervisionRequest request) {
        if (supervisionRepository.existsByContratoIdAndIdSupervisionNot(request.idContrato(), id)) {
            throw new ConflictException("El contrato ya tiene una supervisión asociada");
        }
    }

    @Override
    protected void validarEliminar(Long id, Supervision supervision) {
        if (observacionRepository.existsBySupervisionId(id) || evidenciaRepository.existsBySupervisionId(id)) {
            throw new ConflictException(
                    "No se puede eliminar la supervisión porque tiene observaciones o evidencias asociadas");
        }
    }

    @Override
    protected Supervision toEntity(SupervisionRequest request) {
        // El autor es el usuario autenticado con el token JWT
        Usuario autor = buscarUsuarioPorCorreo(SecurityContextHolder.getContext().getAuthentication().getName());
        return Supervision.builder()
                .fechaRegistro(request.fecha())
                .avancePorcentual(request.avance())
                .observaciones(request.descripcion())
                .contrato(buscarContrato(request.idContrato()))
                .usuario(autor)
                .tipoAutor(autor.getRol().getNombre())
                .build();
    }

    @Override
    protected void actualizarEntidad(Supervision supervision, SupervisionRequest request) {
        supervision.setFechaRegistro(request.fecha());
        supervision.setAvancePorcentual(request.avance());
        supervision.setObservaciones(request.descripcion());
        supervision.setContrato(buscarContrato(request.idContrato()));
    }

    @Override
    protected SupervisionResponse toResponse(Supervision supervision) {
        return SupervisionResponse.from(supervision);
    }

    private Contrato buscarContrato(Long idContrato) {
        return contratoRepository.findById(idContrato)
                .orElseThrow(() -> new RecursoNoEncontradoException("Contrato no encontrado con id " + idContrato));
    }

    private Usuario buscarUsuarioPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario autenticado no encontrado con correo " + correo));
    }
}
