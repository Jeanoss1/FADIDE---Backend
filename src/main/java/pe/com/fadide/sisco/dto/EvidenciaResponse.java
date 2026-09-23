package pe.com.fadide.sisco.dto;

import java.time.LocalDate;

import pe.com.fadide.sisco.model.Evidencia;

public record EvidenciaResponse(
        Long idEvidencia,
        String imagen,
        String descripcion,
        LocalDate fecha,
        Long idSupervision
) {

    // Factory: convierte la entidad en DTO de salida
    public static EvidenciaResponse from(Evidencia evidencia) {
        return new EvidenciaResponse(
                evidencia.getIdEvidencia(),
                evidencia.getImagen(),
                evidencia.getDescripcion(),
                evidencia.getFecha(),
                evidencia.getSupervision().getIdSupervision()
        );
    }
}
