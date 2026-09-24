package pe.com.fadide.sisco.dto;

import pe.com.fadide.sisco.model.Contratista;

public record ContratistaResponse(
        Long idContratista,
        String nombre,
        String ruc
) {

    // Factory: convierte la entidad en DTO de salida
    public static ContratistaResponse from(Contratista contratista) {
        return new ContratistaResponse(
                contratista.getIdContratista(),
                contratista.getNombre(),
                contratista.getRuc()
        );
    }
}
