package pe.com.fadide.sisco.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import pe.com.fadide.sisco.dto.ArchivoGuardado;

/**
 * Almacenamiento de los archivos de evidencia (imágenes, PDF, Word, Excel, PowerPoint, DWG, texto).
 * Valida tipo, contenido y tamaño, y guarda cada archivo con un nombre aleatorio.
 */
public interface AlmacenamientoService {

    long TAMANO_MAXIMO = 10L * 1024 * 1024; // 10 MB

    ArchivoGuardado guardar(MultipartFile archivo);

    Resource cargar(String nombreGuardado);

    void eliminar(String nombreGuardado);
}
