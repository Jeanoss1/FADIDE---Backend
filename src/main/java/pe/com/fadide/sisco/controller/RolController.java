package pe.com.fadide.sisco.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.com.fadide.sisco.dto.RolResponseDTO;
import pe.com.fadide.sisco.service.RolService;

import java.util.List;

// CRUD parcial: los roles solo se listan y eliminan, por eso no hereda de CrudController
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    @GetMapping
    public ResponseEntity<List<RolResponseDTO>> listAll() {
        return ResponseEntity.ok(rolService.listar());
    }

    // Regla: solo el rol ADMIN puede eliminar roles
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        rolService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
