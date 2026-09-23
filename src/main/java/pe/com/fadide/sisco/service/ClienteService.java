package pe.com.fadide.sisco.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.com.fadide.sisco.config.ConflictException;
import pe.com.fadide.sisco.config.ResourceNotFoundException;
import pe.com.fadide.sisco.dto.ClienteRequest;
import pe.com.fadide.sisco.dto.ClienteResponse;
import pe.com.fadide.sisco.model.Cliente;
import pe.com.fadide.sisco.repository.ClienteRepository;

// Patrón Singleton: bean de Spring con una única instancia
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar(String razonSocial) {
        List<Cliente> clientes = (razonSocial == null || razonSocial.isBlank())
                ? clienteRepository.findAll()
                : clienteRepository.buscarPorRazonSocial(razonSocial.trim());
        return clientes.stream().map(ClienteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtener(Long id) {
        return ClienteResponse.from(buscar(id));
    }

    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        if (clienteRepository.existsByNumeroDocumento(request.ruc())) {
            throw new ConflictException("Ya existe un cliente con el RUC " + request.ruc());
        }
        Cliente cliente = Cliente.builder()
                .nombreORazonSocial(request.razonSocial().trim())
                .tipoDocumento("RUC")
                .numeroDocumento(request.ruc())
                .direccion(request.direccion())
                .telefono(request.telefono())
                .build();
        return ClienteResponse.from(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = buscar(id);
        if (clienteRepository.existsByNumeroDocumentoAndIdClienteNot(request.ruc(), id)) {
            throw new ConflictException("Ya existe un cliente con el RUC " + request.ruc());
        }
        cliente.setNombreORazonSocial(request.razonSocial().trim());
        cliente.setNumeroDocumento(request.ruc());
        cliente.setDireccion(request.direccion());
        cliente.setTelefono(request.telefono());
        return ClienteResponse.from(clienteRepository.save(cliente));
    }

    @Transactional
    public void eliminar(Long id) {
        clienteRepository.delete(buscar(id));
    }

    private Cliente buscar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id " + id));
    }
}
