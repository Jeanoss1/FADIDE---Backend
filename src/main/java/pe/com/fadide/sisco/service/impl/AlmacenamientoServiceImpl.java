package pe.com.fadide.sisco.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import pe.com.fadide.sisco.config.BadRequestException;
import pe.com.fadide.sisco.config.ResourceNotFoundException;
import pe.com.fadide.sisco.dto.ArchivoGuardado;
import pe.com.fadide.sisco.model.TipoArchivo;
import pe.com.fadide.sisco.service.AlmacenamientoService;

/**
 * Guarda los archivos de evidencia en una carpeta del servidor (fuera de la aplicación y de la
 * base de datos). Cada archivo se renombra con un UUID, de modo que el nombre que envía el
 * usuario nunca se usa como ruta (evita sobrescrituras y ataques de "path traversal").
 * En Docker la carpeta es un volumen propio, por lo que los archivos sobreviven a los redespliegues.
 */
@Service
public class AlmacenamientoServiceImpl implements AlmacenamientoService {

    private static final int BYTES_FIRMA = 8192;

    private final Path directorio;

    public AlmacenamientoServiceImpl(@Value("${sisco.evidencias.directorio}") String directorio) {
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.directorio);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear la carpeta de evidencias " + this.directorio, e);
        }
    }


    @Override
    public ArchivoGuardado guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BadRequestException("Debe adjuntar un archivo");
        }
        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new BadRequestException("El archivo supera el tamaño máximo de 10 MB");
        }
        String nombreOriginal = limpiarNombre(archivo.getOriginalFilename());
        TipoArchivo tipo = TipoArchivo.porExtension(StringUtils.getFilenameExtension(nombreOriginal))
                .orElseThrow(() -> new BadRequestException(
                        "Tipo de archivo no permitido. Se aceptan: " + TipoArchivo.extensionesPermitidas()));

        try {
            byte[] cabecera;
            try (InputStream in = archivo.getInputStream()) {
                cabecera = in.readNBytes(BYTES_FIRMA);
            }
            if (!tipo.coincideFirma(cabecera)) {
                throw new BadRequestException("El contenido del archivo no corresponde a un ." + tipo.extension());
            }
            String nombreGuardado = UUID.randomUUID() + "." + tipo.extension();
            try (InputStream in = archivo.getInputStream()) {
                Files.copy(in, resolver(nombreGuardado), StandardCopyOption.REPLACE_EXISTING);
            }
            return new ArchivoGuardado(nombreGuardado, nombreOriginal, tipo, archivo.getSize());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el archivo", e);
        }
    }

    @Override
    public Resource cargar(String nombreGuardado) {
        Path ruta = resolver(nombreGuardado);
        if (!Files.isRegularFile(ruta)) {
            throw new ResourceNotFoundException("El archivo de la evidencia no existe en el servidor");
        }
        return new PathResource(ruta);
    }

    @Override
    public void eliminar(String nombreGuardado) {
        try {
            Files.deleteIfExists(resolver(nombreGuardado));
        } catch (IOException e) {
            // No se interrumpe la operación: el registro ya se eliminó
        }
    }

    // La ruta final siempre queda dentro de la carpeta de evidencias
    private Path resolver(String nombreGuardado) {
        Path ruta = directorio.resolve(nombreGuardado).normalize();
        if (!ruta.startsWith(directorio)) {
            throw new BadRequestException("Nombre de archivo no válido");
        }
        return ruta;
    }

    // Conserva solo el nombre (sin carpetas) y quita caracteres de control; máximo 200 caracteres
    private static String limpiarNombre(String nombre) {
        String limpio = StringUtils.getFilename(StringUtils.cleanPath(nombre == null ? "" : nombre));
        limpio = limpio == null ? "" : limpio.replaceAll("[\\p{Cntrl}\\\\/:*?\"<>|]", "_").trim();
        if (limpio.isBlank()) {
            throw new BadRequestException("El archivo no tiene un nombre válido");
        }
        if (limpio.length() > 200) {
            String ext = StringUtils.getFilenameExtension(limpio);
            limpio = limpio.substring(0, 190) + (ext != null ? "." + ext : "");
        }
        return limpio;
    }
}
