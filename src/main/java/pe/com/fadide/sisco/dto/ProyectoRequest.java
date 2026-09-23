package pe.com.fadide.sisco.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.com.fadide.sisco.model.EstadoProyecto;

public record ProyectoRequest(
        @NotBlank @Size(max = 150) String nombre,
        @NotBlank @Size(max = 200) String ubicacion,
        EstadoProyecto estado,
        @NotNull Long idCliente,
        Set<Long> idContratistas
) {
}
