package pe.com.fadide.sisco.service.impl;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.com.fadide.sisco.common.service.impl.CrudServiceImpl;
import pe.com.fadide.sisco.config.ConflictException;
import pe.com.fadide.sisco.dto.ContratistaRequest;
import pe.com.fadide.sisco.dto.ContratistaResponse;
import pe.com.fadide.sisco.model.Contratista;
import pe.com.fadide.sisco.repository.ContratistaRepository;
import pe.com.fadide.sisco.service.ContratistaService;

// Patrón Singleton: bean de Spring con una única instancia
@Service
@RequiredArgsConstructor
public class ContratistaServiceImpl
        extends CrudServiceImpl<Contratista, ContratistaRequest, ContratistaResponse, Long>
        implements ContratistaService {

    private final ContratistaRepository contratistaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ContratistaResponse> listar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return listar();
        }
        return contratistaRepository.buscarPorNombre(nombre.trim()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    protected JpaRepository<Contratista, Long> getRepository() {
        return contratistaRepository;
    }

    @Override
    protected String mensajeNoEncontrado(Long id) {
        return "Contratista no encontrado con id " + id;
    }

    @Override
    protected void validarCrear(ContratistaRequest request) {
        if (contratistaRepository.existsByRuc(request.ruc())) {
            throw new ConflictException("Ya existe un contratista con el RUC " + request.ruc());
        }
    }

    @Override
    protected void validarActualizar(Long id, ContratistaRequest request) {
        if (contratistaRepository.existsByRucAndIdContratistaNot(request.ruc(), id)) {
            throw new ConflictException("Ya existe un contratista con el RUC " + request.ruc());
        }
    }

    @Override
    protected Contratista toEntity(ContratistaRequest request) {
        return Contratista.builder()
                .nombre(request.nombre().trim())
                .ruc(request.ruc())
                .build();
    }

    @Override
    protected void actualizarEntidad(Contratista contratista, ContratistaRequest request) {
        contratista.setNombre(request.nombre().trim());
        contratista.setRuc(request.ruc());
    }

    @Override
    protected ContratistaResponse toResponse(Contratista contratista) {
        return ContratistaResponse.from(contratista);
    }
}
