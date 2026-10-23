package pe.com.fadide.sisco.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.com.fadide.sisco.common.controller.CrudController;
import pe.com.fadide.sisco.dto.ContratoRequestDTO;
import pe.com.fadide.sisco.dto.ContratoResponseDTO;
import pe.com.fadide.sisco.service.ContratoService;

import java.util.List;

// CRUD parcial: el backend no permite modificar contratos (sin PUT), por eso no hereda de CrudController
@RestController
@RequestMapping("/api/contratos")
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;

    @GetMapping
    public ResponseEntity<List<ContratoResponseDTO>> listAll() {
        return ResponseEntity.ok(contratoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContratoResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(contratoService.buscarPorId(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PostMapping
    public ResponseEntity<ContratoResponseDTO> create(@Valid @RequestBody ContratoRequestDTO dto) {
        ContratoResponseDTO creado = contratoService.crear(dto);
        return CrudController.creado(creado, creado.getIdContrato());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contratoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
