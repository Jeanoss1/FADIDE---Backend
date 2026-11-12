package pe.com.fadide.sisco.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.fadide.sisco.config.ResourceNotFoundException;
import pe.com.fadide.sisco.dto.ContratoResponseDTO;
import pe.com.fadide.sisco.dto.EvidenciaResponse;
import pe.com.fadide.sisco.dto.LicenciaResponse;
import pe.com.fadide.sisco.dto.MiObraResponse;
import pe.com.fadide.sisco.dto.ObservacionResponse;
import pe.com.fadide.sisco.dto.ProyectoResponse;
import pe.com.fadide.sisco.dto.SupervisionResponse;
import pe.com.fadide.sisco.model.Cliente;
import pe.com.fadide.sisco.model.Contrato;
import pe.com.fadide.sisco.model.Proyecto;
import pe.com.fadide.sisco.model.Usuario;
import pe.com.fadide.sisco.repository.ClienteRepository;
import pe.com.fadide.sisco.repository.ContratoRepository;
import pe.com.fadide.sisco.repository.EvidenciaRepository;
import pe.com.fadide.sisco.repository.LicenciaRepository;
import pe.com.fadide.sisco.repository.ObservacionRepository;
import pe.com.fadide.sisco.repository.ProyectoRepository;
import pe.com.fadide.sisco.repository.SupervisionRepository;
import pe.com.fadide.sisco.repository.UsuarioRepository;
import pe.com.fadide.sisco.service.PortalService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortalServiceImpl implements PortalService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final ProyectoRepository proyectoRepository;
    private final LicenciaRepository licenciaRepository;
    private final ContratoRepository contratoRepository;
    private final SupervisionRepository supervisionRepository;
    private final EvidenciaRepository evidenciaRepository;
    private final ObservacionRepository observacionRepository;

    @Override
    public List<MiObraResponse> misObras(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        // El documento del usuario CLIENTE es el RUC del cliente registrado
        Cliente cliente = clienteRepository.findByRuc(usuario.getDocumento())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el cliente asociado a tu cuenta"));

        return proyectoRepository.buscarPorCliente(cliente.getIdCliente()).stream()
                .map(this::toMiObra)
                .toList();
    }

    private MiObraResponse toMiObra(Proyecto proyecto) {
        LicenciaResponse licencia = licenciaRepository.buscarPorProyecto(proyecto.getIdProyecto())
                .map(LicenciaResponse::from)
                .orElse(null);

        List<MiObraResponse.ContratoAvance> contratos = contratoRepository.buscarPorProyecto(proyecto.getIdProyecto())
                .stream()
                .map(this::toContratoAvance)
                .toList();

        return new MiObraResponse(ProyectoResponse.from(proyecto), licencia, contratos);
    }

    private MiObraResponse.ContratoAvance toContratoAvance(Contrato contrato) {
        return supervisionRepository.buscarPorContrato(contrato.getIdContrato())
                .map(s -> new MiObraResponse.ContratoAvance(
                        toContratoDto(contrato),
                        SupervisionResponse.from(s),
                        evidenciaRepository.buscarPorSupervision(s.getIdSupervision()).stream()
                                .map(EvidenciaResponse::from).toList(),
                        observacionRepository.buscarPorSupervision(s.getIdSupervision()).stream()
                                .map(ObservacionResponse::from).toList()))
                .orElseGet(() -> new MiObraResponse.ContratoAvance(toContratoDto(contrato), null, List.of(), List.of()));
    }

    private ContratoResponseDTO toContratoDto(Contrato c) {
        return ContratoResponseDTO.builder()
                .idContrato(c.getIdContrato())
                .numeroContrato(c.getNumeroContrato())
                .fechaInicio(c.getFechaInicio())
                .fechaFin(c.getFechaFin())
                .montoTotal(c.getMontoTotal())
                .estado(c.getEstado())
                .idProyecto(c.getProyecto().getIdProyecto())
                .build();
    }
}
