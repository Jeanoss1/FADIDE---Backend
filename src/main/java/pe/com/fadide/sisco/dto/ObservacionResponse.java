package pe.com.fadide.sisco.dto;

import java.time.LocalDate;

import pe.com.fadide.sisco.model.Observacion;

public record ObservacionResponse(
        Long idObservacion,
        String descripcion,
        LocalDate fecha,
        Long idSupervision
) {

    // Factory: convierte la entidad en DTO de salida
    public static ObservacionResponse from(Observacion observacion) {
        return new ObservacionResponse(
                observacion.getIdObservacion(),
                observacion.getDescripcion(),
                observacion.getFecha(),
                observacion.getSupervision().getIdSupervision()
        );
    }
}
