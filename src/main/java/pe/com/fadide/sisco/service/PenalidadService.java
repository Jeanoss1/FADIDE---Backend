package pe.com.fadide.sisco.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import pe.com.fadide.sisco.dto.FiltroBusqueda;
import pe.com.fadide.sisco.dto.PageResponse;
import pe.com.fadide.sisco.dto.PenalidadRequestDTO;
import pe.com.fadide.sisco.dto.PenalidadResponseDTO;

public interface PenalidadService {
    List<PenalidadResponseDTO> findAll();
    PenalidadResponseDTO findById(Long id);
    PenalidadResponseDTO create(PenalidadRequestDTO dto);

    PageResponse<PenalidadResponseDTO> buscar(FiltroBusqueda filtro, Pageable pageable);
}
