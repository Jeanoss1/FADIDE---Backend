package pe.com.fadide.sisco.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import pe.com.fadide.sisco.dto.EvidenciaRequest;
import pe.com.fadide.sisco.dto.EvidenciaResponse;
import pe.com.fadide.sisco.service.EvidenciaService;

@RestController
@RequestMapping("/api/evidencias")
public class EvidenciaController {

    private final EvidenciaService evidenciaService;

    public EvidenciaController(EvidenciaService evidenciaService) {
        this.evidenciaService = evidenciaService;
    }

    @GetMapping
    public ResponseEntity<List<EvidenciaResponse>> listar(@RequestParam(required = false) Long idSupervision) {
        return ResponseEntity.ok(evidenciaService.listar(idSupervision));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EvidenciaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(evidenciaService.obtener(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    @PostMapping
    public ResponseEntity<EvidenciaResponse> crear(@Valid @RequestBody EvidenciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evidenciaService.crear(request));
    }

    /**
     * Sube un archivo como evidencia (multipart/form-data). Se aceptan imágenes (jpg, png, gif, webp),
     * PDF, Word, Excel, PowerPoint, planos DWG y texto (txt, csv), de hasta 10 MB.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    @PostMapping(value = "/archivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EvidenciaResponse> subirArchivo(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(required = false) String descripcion,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam Long idSupervision) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(evidenciaService.crearConArchivo(archivo, descripcion, fecha, idSupervision));
    }

    /**
     * Descarga el archivo de una evidencia. Imágenes y PDF se muestran en el navegador (inline);
     * Word, Excel y demás se descargan (attachment). El Content-Type sale de la lista blanca del
     * servidor, nunca del valor enviado por el usuario.
     */
    @GetMapping("/{id}/archivo")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable Long id) {
        EvidenciaService.ArchivoEvidencia archivo = evidenciaService.descargar(id);
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

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    @PutMapping("/{id}")
    public ResponseEntity<EvidenciaResponse> actualizar(@PathVariable Long id,
                                                        @Valid @RequestBody EvidenciaRequest request) {
        return ResponseEntity.ok(evidenciaService.actualizar(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        evidenciaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
