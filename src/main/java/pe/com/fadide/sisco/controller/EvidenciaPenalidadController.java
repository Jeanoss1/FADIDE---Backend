package pe.com.fadide.sisco.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import pe.com.fadide.sisco.dto.EvidenciaPenalidadResponseDTO;
import pe.com.fadide.sisco.service.EvidenciaPenalidadService;
import pe.com.fadide.sisco.util.RespuestaArchivo;

/**
 * Evidencias (documentos de sustento) de las penalidades: actas, informes, cartas, fotos, hojas de
 * cálculo, etc. Mismo módulo de gestión que las penalidades: solo ADMIN y GESTOR (SecurityConfig).
 */
@RestController
@RequestMapping("/api/penalidades")
@RequiredArgsConstructor
public class EvidenciaPenalidadController {

    private final EvidenciaPenalidadService evidenciaPenalidadService;

    @GetMapping("/{idPenalidad}/evidencias")
    public ResponseEntity<List<EvidenciaPenalidadResponseDTO>> listar(@PathVariable Long idPenalidad) {
        return ResponseEntity.ok(evidenciaPenalidadService.listar(idPenalidad));
    }

    // multipart/form-data: archivo (hasta 10 MB), fecha (yyyy-MM-dd) y descripción opcional
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PostMapping(value = "/{idPenalidad}/evidencias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EvidenciaPenalidadResponseDTO> subir(
            @PathVariable Long idPenalidad,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(required = false) String descripcion,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(evidenciaPenalidadService.subir(idPenalidad, archivo, descripcion, fecha));
    }

    @GetMapping("/evidencias/{idEvidencia}/archivo")
    public ResponseEntity<Resource> descargar(@PathVariable Long idEvidencia) {
        return RespuestaArchivo.de(evidenciaPenalidadService.descargar(idEvidencia));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @DeleteMapping("/evidencias/{idEvidencia}")
    public ResponseEntity<Void> eliminar(@PathVariable Long idEvidencia) {
        evidenciaPenalidadService.eliminar(idEvidencia);
        return ResponseEntity.noContent().build();
    }
}
