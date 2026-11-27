package pe.com.fadide.sisco.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import pe.com.fadide.sisco.dto.ArchivoDescarga;
import pe.com.fadide.sisco.dto.EvidenciaPenalidadResponseDTO;

public interface EvidenciaPenalidadService {

    List<EvidenciaPenalidadResponseDTO> listar(Long idPenalidad);

    EvidenciaPenalidadResponseDTO subir(Long idPenalidad, MultipartFile archivo, String descripcion, LocalDate fecha);

    ArchivoDescarga descargar(Long idEvidenciaPenalidad);

    void eliminar(Long idEvidenciaPenalidad);
}
