package pe.com.fadide.sisco.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<EvidenciaResponse> crear(@Valid @RequestBody EvidenciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evidenciaService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EvidenciaResponse> actualizar(@PathVariable Long id,
                                                        @Valid @RequestBody EvidenciaRequest request) {
        return ResponseEntity.ok(evidenciaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        evidenciaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
