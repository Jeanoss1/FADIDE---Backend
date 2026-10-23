package pe.com.fadide.sisco.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.com.fadide.sisco.common.controller.CrudController;
import pe.com.fadide.sisco.dto.PenalidadRequestDTO;
import pe.com.fadide.sisco.dto.PenalidadResponseDTO;
import pe.com.fadide.sisco.service.PenalidadService;

import java.util.List;

// CRUD parcial: el backend solo permite consultar y registrar penalidades, por eso no hereda de CrudController
@RestController
@RequestMapping("/api/penalidades")
@RequiredArgsConstructor
public class PenalidadController {

    private final PenalidadService penalidadService;

    @GetMapping
    public ResponseEntity<List<PenalidadResponseDTO>> listAll() {
        return ResponseEntity.ok(penalidadService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PenalidadResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(penalidadService.buscarPorId(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PostMapping
    public ResponseEntity<PenalidadResponseDTO> create(@Valid @RequestBody PenalidadRequestDTO dto) {
        PenalidadResponseDTO creado = penalidadService.crear(dto);
        return CrudController.creado(creado, creado.getIdPenalidad());
    }
}
