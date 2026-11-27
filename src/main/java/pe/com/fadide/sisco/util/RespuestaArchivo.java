package pe.com.fadide.sisco.util;

import java.nio.charset.StandardCharsets;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import pe.com.fadide.sisco.dto.ArchivoDescarga;

/**
 * Arma la respuesta HTTP de descarga de una evidencia. Imágenes y PDF se muestran en el navegador
 * (inline); Word, Excel y demás se descargan (attachment). El Content-Type sale de la lista blanca
 * del servidor (TipoArchivo), nunca del valor enviado por el usuario, y "nosniff" impide que el
 * navegador lo reinterprete.
 */
public final class RespuestaArchivo {

    private RespuestaArchivo() {
    }

    public static ResponseEntity<Resource> de(ArchivoDescarga archivo) {
        ContentDisposition disposicion = (archivo.tipo().visibleEnNavegador()
                ? ContentDisposition.inline() : ContentDisposition.attachment())
                .filename(archivo.nombre(), StandardCharsets.UTF_8)
                .build();
        ResponseEntity.BodyBuilder respuesta = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.tipo().contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString())
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.noStore());
        if (archivo.tamano() >= 0) {
            respuesta.contentLength(archivo.tamano());
        }
        return respuesta.body(archivo.recurso());
    }
}
