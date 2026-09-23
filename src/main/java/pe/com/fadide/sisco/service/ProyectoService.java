package pe.com.fadide.sisco.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.com.fadide.sisco.config.ConflictException;
import pe.com.fadide.sisco.config.ResourceNotFoundException;
import pe.com.fadide.sisco.dto.ProyectoRequest;
import pe.com.fadide.sisco.dto.ProyectoResponse;
import pe.com.fadide.sisco.model.Cliente;
import pe.com.fadide.sisco.model.Contratista;
import pe.com.fadide.sisco.model.EstadoProyecto;
import pe.com.fadide.sisco.model.Proyecto;
import pe.com.fadide.sisco.repository.ClienteRepository;
import pe.com.fadide.sisco.repository.ContratistaRepository;
import pe.com.fadide.sisco.repository.ContratoRepository;
import pe.com.fadide.sisco.repository.LicenciaRepository;
import pe.com.fadide.sisco.repository.ProyectoRepository;

// Patrón Singleton: bean de Spring con una única instancia
@Service
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;
    private final ClienteRepository clienteRepository;
    private final ContratistaRepository contratistaRepository;
    private final LicenciaRepository licenciaRepository;
    private final ContratoRepository contratoRepository;

    public ProyectoService(ProyectoRepository proyectoRepository,
                           ClienteRepository clienteRepository,
                           ContratistaRepository contratistaRepository,
                           LicenciaRepository licenciaRepository,
                           ContratoRepository contratoRepository) {
        this.proyectoRepository = proyectoRepository;
        this.clienteRepository = clienteRepository;
        this.contratistaRepository = contratistaRepository;
        this.licenciaRepository = licenciaRepository;
        this.contratoRepository = contratoRepository;
    }

    @Transactional(readOnly = true)
    public List<ProyectoResponse> listar(EstadoProyecto estado, Long idCliente) {
        List<Proyecto> proyectos;
        if (estado != null && idCliente != null) {
            proyectos = proyectoRepository.buscarPorEstadoYCliente(estado, idCliente);
        } else if (estado != null) {
            proyectos = proyectoRepository.buscarPorEstado(estado);
        } else if (idCliente != null) {
            proyectos = proyectoRepository.buscarPorCliente(idCliente);
        } else {
            proyectos = proyectoRepository.listarTodos();
        }
        return proyectos.stream().map(ProyectoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProyectoResponse obtener(Long id) {
        return ProyectoResponse.from(buscar(id));
    }

    @Transactional
    public ProyectoResponse crear(ProyectoRequest request) {
        Proyecto proyecto = Proyecto.builder()
                .nombre(request.nombre().trim())
                .ubicacion(request.ubicacion().trim())
                .estado(request.estado() != null ? request.estado() : EstadoProyecto.PLANIFICADO)
                .cliente(buscarCliente(request.idCliente()))
                .contratistas(buscarContratistas(request.idContratistas()))
                .build();
        return ProyectoResponse.from(proyectoRepository.save(proyecto));
    }

    @Transactional
    public ProyectoResponse actualizar(Long id, ProyectoRequest request) {
        Proyecto proyecto = buscar(id);
        proyecto.setNombre(request.nombre().trim());
        proyecto.setUbicacion(request.ubicacion().trim());
        if (request.estado() != null) {
            proyecto.setEstado(request.estado());
        }
        proyecto.setCliente(buscarCliente(request.idCliente()));
        proyecto.setContratistas(buscarContratistas(request.idContratistas()));
        return ProyectoResponse.from(proyectoRepository.save(proyecto));
    }

    @Transactional
    public void eliminar(Long id) {
        Proyecto proyecto = buscar(id);
        if (licenciaRepository.existsByProyectoId(id)) {
            throw new ConflictException("No se puede eliminar el proyecto porque tiene una licencia asociada");
        }
        if (contratoRepository.existsByProyectoId(id)) {
            throw new ConflictException("No se puede eliminar el proyecto porque tiene un contrato asociado");
        }
        proyectoRepository.delete(proyecto);
    }

    private Proyecto buscar(Long id) {
        return proyectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con id " + id));
    }

    private Cliente buscarCliente(Long idCliente) {
        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id " + idCliente));
    }

    private Set<Contratista> buscarContratistas(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        List<Contratista> encontrados = contratistaRepository.findAllById(ids);
        if (encontrados.size() != ids.size()) {
            Set<Long> existentes = new HashSet<>();
            encontrados.forEach(c -> existentes.add(c.getIdContratista()));
            Set<Long> faltantes = new HashSet<>(ids);
            faltantes.removeAll(existentes);
            throw new ResourceNotFoundException("Contratistas no encontrados con id " + faltantes);
        }
        return new HashSet<>(encontrados);
    }
}
