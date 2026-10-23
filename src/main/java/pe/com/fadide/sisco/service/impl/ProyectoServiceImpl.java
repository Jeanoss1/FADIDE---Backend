package pe.com.fadide.sisco.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.com.fadide.sisco.common.exception.RecursoNoEncontradoException;
import pe.com.fadide.sisco.common.service.impl.CrudServiceImpl;
import pe.com.fadide.sisco.config.ConflictException;
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
import pe.com.fadide.sisco.service.ProyectoService;

// Patrón Singleton: bean de Spring con una única instancia
@Service
@RequiredArgsConstructor
public class ProyectoServiceImpl extends CrudServiceImpl<Proyecto, ProyectoRequest, ProyectoResponse, Long>
        implements ProyectoService {

    private final ProyectoRepository proyectoRepository;
    private final ClienteRepository clienteRepository;
    private final ContratistaRepository contratistaRepository;
    private final LicenciaRepository licenciaRepository;
    private final ContratoRepository contratoRepository;

    @Override
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
            proyectos = buscarTodos();
        }
        return proyectos.stream().map(this::toResponse).toList();
    }

    @Override
    protected JpaRepository<Proyecto, Long> getRepository() {
        return proyectoRepository;
    }

    @Override
    protected List<Proyecto> buscarTodos() {
        return proyectoRepository.listarTodos();
    }

    @Override
    protected String mensajeNoEncontrado(Long id) {
        return "Proyecto no encontrado con id " + id;
    }

    @Override
    protected void validarEliminar(Long id, Proyecto proyecto) {
        if (licenciaRepository.existsByProyectoId(id)) {
            throw new ConflictException("No se puede eliminar el proyecto porque tiene una licencia asociada");
        }
        if (contratoRepository.existsByProyectoId(id)) {
            throw new ConflictException("No se puede eliminar el proyecto porque tiene un contrato asociado");
        }
    }

    @Override
    protected Proyecto toEntity(ProyectoRequest request) {
        return Proyecto.builder()
                .nombre(request.nombre().trim())
                .ubicacion(request.ubicacion().trim())
                .estado(request.estado() != null ? request.estado() : EstadoProyecto.PLANIFICADO)
                .cliente(buscarCliente(request.idCliente()))
                .contratistas(buscarContratistas(request.idContratistas()))
                .build();
    }

    @Override
    protected void actualizarEntidad(Proyecto proyecto, ProyectoRequest request) {
        proyecto.setNombre(request.nombre().trim());
        proyecto.setUbicacion(request.ubicacion().trim());
        if (request.estado() != null) {
            proyecto.setEstado(request.estado());
        }
        proyecto.setCliente(buscarCliente(request.idCliente()));
        proyecto.setContratistas(buscarContratistas(request.idContratistas()));
    }

    @Override
    protected ProyectoResponse toResponse(Proyecto proyecto) {
        return ProyectoResponse.from(proyecto);
    }

    private Cliente buscarCliente(Long idCliente) {
        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con id " + idCliente));
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
            throw new RecursoNoEncontradoException("Contratistas no encontrados con id " + faltantes);
        }
        return new HashSet<>(encontrados);
    }
}
