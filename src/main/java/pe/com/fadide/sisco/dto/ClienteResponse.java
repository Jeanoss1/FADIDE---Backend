package pe.com.fadide.sisco.dto;

import pe.com.fadide.sisco.model.Cliente;

public record ClienteResponse(
        Long idCliente,
        String razonSocial,
        String ruc,
        String direccion,
        String telefono
) {

    // Factory: convierte la entidad en DTO de salida
    public static ClienteResponse from(Cliente cliente) {
        return new ClienteResponse(
                cliente.getIdCliente(),
                cliente.getNombreORazonSocial(),
                cliente.getNumeroDocumento(),
                cliente.getDireccion(),
                cliente.getTelefono()
        );
    }
}
