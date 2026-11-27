package pe.com.fadide.sisco.dto;

import java.time.LocalDate;

import pe.com.fadide.sisco.model.TipoArchivo;
import pe.com.fadide.sisco.model.Evidencia;

/**
 * Evidencia de una supervisión. Si {@code tieneArchivo} es verdadero, {@code imagen} es el nombre
 * original del archivo y el contenido se descarga en GET /api/evidencias/{id}/archivo;
 * si es falso, {@code imagen} es un enlace (URL) externo.
 * {@code categoria}: IMAGEN, PDF, WORD, EXCEL, POWERPOINT, PLANO, TEXTO o ENLACE.
 */
public record EvidenciaResponse(
        Long idEvidencia,
        String imagen,
        String descripcion,
        LocalDate fecha,
        Long idSupervision,
        boolean tieneArchivo,
        String categoria,
        String tipoContenido,
        Long tamanoBytes
) {

    // Factory: convierte la entidad en DTO de salida
    public static EvidenciaResponse from(Evidencia evidencia) {
        boolean tieneArchivo = evidencia.getArchivo() != null;
        String categoria = tieneArchivo
                ? TipoArchivo.porContentType(evidencia.getTipoContenido()).map(t -> t.categoria().name()).orElse("ARCHIVO")
                : "ENLACE";
        return new EvidenciaResponse(
                evidencia.getIdEvidencia(),
                evidencia.getImagen(),
                evidencia.getDescripcion(),
                evidencia.getFecha(),
                evidencia.getSupervision().getIdSupervision(),
                tieneArchivo,
                categoria,
                evidencia.getTipoContenido(),
                evidencia.getTamanoBytes()
        );
    }
}
