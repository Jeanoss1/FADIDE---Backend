package pe.com.fadide.sisco.dto;

import java.util.Comparator;
import java.util.List;

import pe.com.fadide.sisco.model.EstadoProyecto;
import pe.com.fadide.sisco.model.Proyecto;

public record ProyectoResponse(
        Long idProyecto,
        String nombre,
        String ubicacion,
        EstadoProyecto estado,
        ClienteResponse cliente,
        List<ContratistaResponse> contratistas
) {

    // Factory: convierte la entidad en DTO de salida
    public static ProyectoResponse from(Proyecto proyecto) {
        return new ProyectoResponse(
                proyecto.getIdProyecto(),
                proyecto.getNombre(),
                proyecto.getUbicacion(),
                proyecto.getEstado(),
                ClienteResponse.from(proyecto.getCliente()),
                proyecto.getContratistas().stream()
                        .map(ContratistaResponse::from)
                        .sorted(Comparator.comparing(ContratistaResponse::idContratista))
                        .toList()
        );
    }
}
