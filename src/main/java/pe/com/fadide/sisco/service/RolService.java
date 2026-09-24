package pe.com.fadide.sisco.service;

import pe.com.fadide.sisco.dto.RolResponseDTO;

import java.util.List;

public interface RolService {
    List<RolResponseDTO> findAll();
    void deleteById(Long id);
}
