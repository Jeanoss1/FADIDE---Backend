package pe.com.fadide.sisco.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import pe.com.fadide.sisco.dto.FiltroBusqueda;
import pe.com.fadide.sisco.dto.PageResponse;
import pe.com.fadide.sisco.dto.PagoRequestDTO;
import pe.com.fadide.sisco.dto.PagoResponseDTO;

public interface PagoService {
    List<PagoResponseDTO> findAll();
    PagoResponseDTO findById(Long id);
    PagoResponseDTO create(PagoRequestDTO dto);

    PageResponse<PagoResponseDTO> buscar(FiltroBusqueda filtro, Pageable pageable);
}
