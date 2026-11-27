package pe.com.fadide.sisco.dto;

import org.springframework.core.io.Resource;

import pe.com.fadide.sisco.model.TipoArchivo;

/** Archivo de evidencia listo para enviarse al navegador (contenido, nombre original, tipo y tamaño). */
public record ArchivoDescarga(Resource recurso, String nombre, TipoArchivo tipo, long tamano) {
}
