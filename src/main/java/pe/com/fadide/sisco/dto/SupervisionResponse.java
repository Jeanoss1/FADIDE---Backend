package pe.com.fadide.sisco.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import pe.com.fadide.sisco.model.Supervision;

public record SupervisionResponse(
        Long idSupervision,
        LocalDate fecha,
        BigDecimal avance,
        String descripcion,
        Long idContrato,
        String numeroContrato
) {

    // Factory: convierte la entidad en DTO de salida
    public static SupervisionResponse from(Supervision supervision) {
        return new SupervisionResponse(
                supervision.getIdSupervision(),
                supervision.getFechaRegistro(),
                supervision.getAvancePorcentual(),
                supervision.getObservaciones(),
                supervision.getContrato().getIdContrato(),
                supervision.getContrato().getNumeroContrato()
        );
    }
}
