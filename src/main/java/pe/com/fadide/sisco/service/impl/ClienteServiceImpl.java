package pe.com.fadide.sisco.service.impl;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.com.fadide.sisco.common.service.impl.CrudServiceImpl;
import pe.com.fadide.sisco.config.ConflictException;
import pe.com.fadide.sisco.dto.ClienteRequest;
import pe.com.fadide.sisco.dto.ClienteResponse;
import pe.com.fadide.sisco.model.Cliente;
import pe.com.fadide.sisco.repository.ClienteRepository;
import pe.com.fadide.sisco.service.ClienteService;

// Patrón Singleton: bean de Spring con una única instancia
@Service
@RequiredArgsConstructor
public class ClienteServiceImpl extends CrudServiceImpl<Cliente, ClienteRequest, ClienteResponse, Long>
        implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> listar(String razonSocial) {
        if (razonSocial == null || razonSocial.isBlank()) {
            return listar();
        }
        return clienteRepository.buscarPorRazonSocial(razonSocial.trim()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    protected JpaRepository<Cliente, Long> getRepository() {
        return clienteRepository;
    }

    @Override
    protected String mensajeNoEncontrado(Long id) {
        return "Cliente no encontrado con id " + id;
    }

    @Override
    protected void validarCrear(ClienteRequest request) {
        if (clienteRepository.existsByRuc(request.ruc())) {
            throw new ConflictException("Ya existe un cliente con el RUC " + request.ruc());
        }
    }

    @Override
    protected void validarActualizar(Long id, ClienteRequest request) {
        if (clienteRepository.existsByRucAndIdClienteNot(request.ruc(), id)) {
            throw new ConflictException("Ya existe un cliente con el RUC " + request.ruc());
        }
    }

    @Override
    protected Cliente toEntity(ClienteRequest request) {
        return Cliente.builder()
                .razonSocial(request.razonSocial().trim())
                .ruc(request.ruc())
                .direccion(request.direccion())
                .telefono(request.telefono())
                .build();
    }

    @Override
    protected void actualizarEntidad(Cliente cliente, ClienteRequest request) {
        cliente.setRazonSocial(request.razonSocial().trim());
        cliente.setRuc(request.ruc());
        cliente.setDireccion(request.direccion());
        cliente.setTelefono(request.telefono());
    }

    @Override
    protected ClienteResponse toResponse(Cliente cliente) {
        return ClienteResponse.from(cliente);
    }
}
