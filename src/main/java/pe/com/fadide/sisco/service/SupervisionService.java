package pe.com.fadide.sisco.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.com.fadide.sisco.config.ConflictException;
import pe.com.fadide.sisco.config.ResourceNotFoundException;
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

// Patrón Singleton: bean de Spring con una única instancia
@Service
public class SupervisionService {

    private final SupervisionRepository supervisionRepository;
    private final ContratoRepository contratoRepository;
    private final ObservacionRepository observacionRepository;
    private final EvidenciaRepository evidenciaRepository;
    private final UsuarioRepository usuarioRepository;

    public SupervisionService(SupervisionRepository supervisionRepository,
                              ContratoRepository contratoRepository,
                              ObservacionRepository observacionRepository,
                              EvidenciaRepository evidenciaRepository,
                              UsuarioRepository usuarioRepository) {
        this.supervisionRepository = supervisionRepository;
        this.contratoRepository = contratoRepository;
        this.observacionRepository = observacionRepository;
        this.evidenciaRepository = evidenciaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<SupervisionResponse> listar() {
        return supervisionRepository.listarTodas().stream().map(SupervisionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public SupervisionResponse obtener(Long id) {
        return SupervisionResponse.from(buscar(id));
    }

    @Transactional(readOnly = true)
    public SupervisionResponse obtenerPorContrato(Long idContrato) {
        return supervisionRepository.buscarPorContrato(idContrato)
                .map(SupervisionResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe una supervisión para el contrato con id " + idContrato));
    }

    @Transactional
    public SupervisionResponse crear(SupervisionRequest request, String correoAutenticado) {
        if (supervisionRepository.existsByContratoId(request.idContrato())) {
            throw new ConflictException("El contrato ya tiene una supervisión asociada");
        }
        Usuario autor = buscarUsuarioPorCorreo(correoAutenticado);
        Supervision supervision = Supervision.builder()
                .fechaRegistro(request.fecha())
                .avancePorcentual(request.avance())
                .observaciones(request.descripcion())
                .contrato(buscarContrato(request.idContrato()))
                .usuario(autor)
                .tipoAutor(autor.getRol().getNombre())
                .build();
        return SupervisionResponse.from(supervisionRepository.save(supervision));
    }

    @Transactional
    public SupervisionResponse actualizar(Long id, SupervisionRequest request) {
        Supervision supervision = buscar(id);
        if (supervisionRepository.existsByContratoIdAndIdSupervisionNot(request.idContrato(), id)) {
            throw new ConflictException("El contrato ya tiene una supervisión asociada");
        }
        supervision.setFechaRegistro(request.fecha());
        supervision.setAvancePorcentual(request.avance());
        supervision.setObservaciones(request.descripcion());
        supervision.setContrato(buscarContrato(request.idContrato()));
        return SupervisionResponse.from(supervisionRepository.save(supervision));
    }

    @Transactional
    public void eliminar(Long id) {
        Supervision supervision = buscar(id);
        if (observacionRepository.existsBySupervisionId(id) || evidenciaRepository.existsBySupervisionId(id)) {
            throw new ConflictException(
                    "No se puede eliminar la supervisión porque tiene observaciones o evidencias asociadas");
        }
        supervisionRepository.delete(supervision);
    }

    private Supervision buscar(Long id) {
        return supervisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supervisión no encontrada con id " + id));
    }

    private Contrato buscarContrato(Long idContrato) {
        return contratoRepository.findById(idContrato)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado con id " + idContrato));
    }

    private Usuario buscarUsuarioPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado con correo " + correo));
    }
}
