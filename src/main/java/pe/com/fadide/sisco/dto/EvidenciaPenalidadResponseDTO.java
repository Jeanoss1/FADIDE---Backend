package pe.com.fadide.sisco.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Evidencia (documento de sustento) de una penalidad. El contenido se descarga en
 * GET /api/penalidades/evidencias/{idEvidenciaPenalidad}/archivo.
 * categoria: IMAGEN, PDF, WORD, EXCEL, POWERPOINT, PLANO o TEXTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvidenciaPenalidadResponseDTO {
    private Long idEvidenciaPenalidad;
    private String nombreOriginal;
    private String categoria;
    private String tipoContenido;
    private Long tamanoBytes;
    private String descripcion;
    private LocalDate fecha;
    private Long idPenalidad;
}
