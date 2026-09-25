package pe.com.fadide.sisco.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.com.fadide.sisco.dto.ContratoRequestDTO;
import pe.com.fadide.sisco.dto.ContratoResponseDTO;
import pe.com.fadide.sisco.service.ContratoService;

import java.util.List;

@RestController
@RequestMapping("/api/contratos")
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;

    @GetMapping
    public ResponseEntity<List<ContratoResponseDTO>> listAll() {
        return ResponseEntity.ok(contratoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContratoResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(contratoService.findById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PostMapping
    public ResponseEntity<ContratoResponseDTO> create(@Valid @RequestBody ContratoRequestDTO dto) {
        return new ResponseEntity<>(contratoService.create(dto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contratoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}