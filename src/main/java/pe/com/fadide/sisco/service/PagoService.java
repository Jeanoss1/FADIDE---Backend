package pe.com.fadide.sisco.service;

import pe.com.fadide.sisco.dto.PagoRequestDTO;
import pe.com.fadide.sisco.dto.PagoResponseDTO;

import java.util.List;

public interface PagoService {
    List<PagoResponseDTO> findAll();
    PagoResponseDTO findById(Long id);
    PagoResponseDTO create(PagoRequestDTO dto);
}