package pe.com.fadide.sisco.dto;

import java.time.LocalDate;

import pe.com.fadide.sisco.model.Licencia;

public record LicenciaResponse(
        Long idLicencia,
        String numero,
        LocalDate fechaEmision,
        LocalDate fechaVencimiento,
        Long idProyecto,
        String nombreProyecto
) {

    // Factory: convierte la entidad en DTO de salida
    public static LicenciaResponse from(Licencia licencia) {
        return new LicenciaResponse(
                licencia.getIdLicencia(),
                licencia.getNumero(),
                licencia.getFechaEmision(),
                licencia.getFechaVencimiento(),
                licencia.getProyecto().getIdProyecto(),
                licencia.getProyecto().getNombre()
        );
    }
}
