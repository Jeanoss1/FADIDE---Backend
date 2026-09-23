package pe.com.fadide.sisco.service;

import pe.com.fadide.sisco.dto.PenalidadRequestDTO;
import pe.com.fadide.sisco.dto.PenalidadResponseDTO;

import java.util.List;

public interface PenalidadService {
    List<PenalidadResponseDTO> findAll();
    PenalidadResponseDTO findById(Long id);
    PenalidadResponseDTO create(PenalidadRequestDTO dto);
}