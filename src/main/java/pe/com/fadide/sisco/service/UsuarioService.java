package pe.com.fadide.sisco.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import pe.com.fadide.sisco.dto.FiltroBusqueda;
import pe.com.fadide.sisco.dto.PageResponse;
import pe.com.fadide.sisco.dto.UsuarioRequestDTO;
import pe.com.fadide.sisco.dto.UsuarioResponseDTO;

public interface UsuarioService {
    List<UsuarioResponseDTO> findAll();
    UsuarioResponseDTO create(UsuarioRequestDTO dto);

    PageResponse<UsuarioResponseDTO> buscar(FiltroBusqueda filtro, Pageable pageable);
}
