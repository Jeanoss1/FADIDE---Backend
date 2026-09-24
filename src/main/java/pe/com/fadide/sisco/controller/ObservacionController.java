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
import pe.com.fadide.sisco.dto.ObservacionRequest;
import pe.com.fadide.sisco.dto.ObservacionResponse;
import pe.com.fadide.sisco.service.ObservacionService;

@RestController
@RequestMapping("/api/observaciones")
public class ObservacionController {

    private final ObservacionService observacionService;

    public ObservacionController(ObservacionService observacionService) {
        this.observacionService = observacionService;
    }

    @GetMapping
    public ResponseEntity<List<ObservacionResponse>> listar(@RequestParam(required = false) Long idSupervision) {
        return ResponseEntity.ok(observacionService.listar(idSupervision));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ObservacionResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(observacionService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<ObservacionResponse> crear(@Valid @RequestBody ObservacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(observacionService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ObservacionResponse> actualizar(@PathVariable Long id,
                                                          @Valid @RequestBody ObservacionRequest request) {
        return ResponseEntity.ok(observacionService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        observacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
