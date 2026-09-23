package pe.com.fadide.sisco.service;

import pe.com.fadide.sisco.dto.ContratoRequestDTO;
import pe.com.fadide.sisco.dto.ContratoResponseDTO;

import java.util.List;

public interface ContratoService {
    List<ContratoResponseDTO> findAll();
    ContratoResponseDTO findById(Long id);
    ContratoResponseDTO create(ContratoRequestDTO dto);
    void deleteById(Long id);
}