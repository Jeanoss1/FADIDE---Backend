package pe.com.fadide.sisco.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import pe.com.fadide.sisco.model.EstadoProyecto;

public record ProyectoRequest(
        @NotBlank(message = "El nombre de la obra es obligatorio")
        @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
        String nombre,

        @NotBlank(message = "La ubicación es obligatoria")
        @Size(min = 3, max = 200, message = "La ubicación debe tener entre 3 y 200 caracteres")
        String ubicacion,

        EstadoProyecto estado,

        @NotNull(message = "El cliente es obligatorio")
        @Positive(message = "El id del cliente no es válido")
        Long idCliente,

        Set<@Positive(message = "El id del contratista no es válido") Long> idContratistas
) {
}
