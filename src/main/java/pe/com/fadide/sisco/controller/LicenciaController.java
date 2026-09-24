package pe.com.fadide.sisco.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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

import jakarta.validation.Valid;
import pe.com.fadide.sisco.dto.LicenciaRequest;
import pe.com.fadide.sisco.dto.LicenciaResponse;
import pe.com.fadide.sisco.service.LicenciaService;

@RestController
@RequestMapping("/api/licencias")
public class LicenciaController {

    private final LicenciaService licenciaService;

    public LicenciaController(LicenciaService licenciaService) {
        this.licenciaService = licenciaService;
    }

    @GetMapping
    public ResponseEntity<List<LicenciaResponse>> listar() {
        return ResponseEntity.ok(licenciaService.listar());
    }

    @GetMapping("/por-vencer")
    public ResponseEntity<List<LicenciaResponse>> listarPorVencer(@RequestParam(defaultValue = "30") int dias) {
        return ResponseEntity.ok(licenciaService.listarPorVencer(dias));
    }

    @GetMapping("/proyecto/{idProyecto}")
    public ResponseEntity<LicenciaResponse> obtenerPorProyecto(@PathVariable Long idProyecto) {
        return ResponseEntity.ok(licenciaService.obtenerPorProyecto(idProyecto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LicenciaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(licenciaService.obtener(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PostMapping
    public ResponseEntity<LicenciaResponse> crear(@Valid @RequestBody LicenciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(licenciaService.crear(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<LicenciaResponse> actualizar(@PathVariable Long id,
                                                       @Valid @RequestBody LicenciaRequest request) {
        return ResponseEntity.ok(licenciaService.actualizar(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        licenciaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
