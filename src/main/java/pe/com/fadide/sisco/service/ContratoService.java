package pe.com.fadide.sisco.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import pe.com.fadide.sisco.dto.ContratoRequestDTO;
import pe.com.fadide.sisco.dto.ContratoResponseDTO;
import pe.com.fadide.sisco.dto.FiltroBusqueda;
import pe.com.fadide.sisco.dto.PageResponse;

public interface ContratoService {
    List<ContratoResponseDTO> findAll();
    ContratoResponseDTO findById(Long id);
    ContratoResponseDTO create(ContratoRequestDTO dto);
    void deleteById(Long id);

    PageResponse<ContratoResponseDTO> buscar(FiltroBusqueda filtro, Pageable pageable);
}
