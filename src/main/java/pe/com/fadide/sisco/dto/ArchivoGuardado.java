package pe.com.fadide.sisco.dto;

import pe.com.fadide.sisco.model.TipoArchivo;

/** Resultado de guardar un archivo de evidencia en el servidor. */
public record ArchivoGuardado(String nombreGuardado, String nombreOriginal, TipoArchivo tipo, long tamano) {
}
