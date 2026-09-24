package pe.com.fadide.sisco.service;

import pe.com.fadide.sisco.dto.UsuarioRequestDTO;
import pe.com.fadide.sisco.dto.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioService {
    List<UsuarioResponseDTO> findAll();
    UsuarioResponseDTO create(UsuarioRequestDTO dto);
}
