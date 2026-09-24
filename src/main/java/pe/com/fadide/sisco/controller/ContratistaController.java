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
import pe.com.fadide.sisco.dto.ContratistaRequest;
import pe.com.fadide.sisco.dto.ContratistaResponse;
import pe.com.fadide.sisco.service.ContratistaService;

@RestController
@RequestMapping("/api/contratistas")
public class ContratistaController {

    private final ContratistaService contratistaService;

    public ContratistaController(ContratistaService contratistaService) {
        this.contratistaService = contratistaService;
    }

    @GetMapping
    public ResponseEntity<List<ContratistaResponse>> listar(@RequestParam(required = false) String nombre) {
        return ResponseEntity.ok(contratistaService.listar(nombre));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContratistaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(contratistaService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<ContratistaResponse> crear(@Valid @RequestBody ContratistaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contratistaService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContratistaResponse> actualizar(@PathVariable Long id,
                                                          @Valid @RequestBody ContratistaRequest request) {
        return ResponseEntity.ok(contratistaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        contratistaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
