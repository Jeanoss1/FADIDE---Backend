package pe.com.fadide.sisco.exportacion;

import org.springframework.http.MediaType;

import pe.com.fadide.sisco.dto.TablaReporte;

/**
 * Patrón Strategy: cada formato de exportación (PDF, Excel) es una estrategia intercambiable.
 * El controlador elige la estrategia por el parámetro ?formato= sin conocer cómo se genera
 * el archivo; agregar un formato nuevo (por ejemplo CSV) solo requiere otra implementación.
 */
public interface ExportadorReporte {

    // Valor del parámetro ?formato= que atiende esta estrategia: "pdf" o "excel"
    String formato();

    MediaType tipoContenido();

    String extension();

    byte[] exportar(TablaReporte reporte);
}
