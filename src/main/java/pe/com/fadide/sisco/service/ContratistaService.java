package pe.com.fadide.sisco.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.com.fadide.sisco.config.ConflictException;
import pe.com.fadide.sisco.config.ResourceNotFoundException;
import pe.com.fadide.sisco.dto.ContratistaRequest;
import pe.com.fadide.sisco.dto.ContratistaResponse;
import pe.com.fadide.sisco.model.Contratista;
import pe.com.fadide.sisco.repository.ContratistaRepository;

// Patrón Singleton: bean de Spring con una única instancia
@Service
public class ContratistaService {

    private final ContratistaRepository contratistaRepository;

    public ContratistaService(ContratistaRepository contratistaRepository) {
        this.contratistaRepository = contratistaRepository;
    }

    @Transactional(readOnly = true)
    public List<ContratistaResponse> listar(String nombre) {
        List<Contratista> contratistas = (nombre == null || nombre.isBlank())
                ? contratistaRepository.findAll()
                : contratistaRepository.buscarPorNombre(nombre.trim());
        return contratistas.stream().map(ContratistaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ContratistaResponse obtener(Long id) {
        return ContratistaResponse.from(buscar(id));
    }

    @Transactional
    public ContratistaResponse crear(ContratistaRequest request) {
        if (contratistaRepository.existsByRuc(request.ruc())) {
            throw new ConflictException("Ya existe un contratista con el RUC " + request.ruc());
        }
        Contratista contratista = Contratista.builder()
                .razonSocial(request.nombre().trim())
                .ruc(request.ruc())
                .build();
        return ContratistaResponse.from(contratistaRepository.save(contratista));
    }

    @Transactional
    public ContratistaResponse actualizar(Long id, ContratistaRequest request) {
        Contratista contratista = buscar(id);
        if (contratistaRepository.existsByRucAndIdContratistaNot(request.ruc(), id)) {
            throw new ConflictException("Ya existe un contratista con el RUC " + request.ruc());
        }
        contratista.setRazonSocial(request.nombre().trim());
        contratista.setRuc(request.ruc());
        return ContratistaResponse.from(contratistaRepository.save(contratista));
    }

    @Transactional
    public void eliminar(Long id) {
        contratistaRepository.delete(buscar(id));
    }

    private Contratista buscar(Long id) {
        return contratistaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contratista no encontrado con id " + id));
    }
}
