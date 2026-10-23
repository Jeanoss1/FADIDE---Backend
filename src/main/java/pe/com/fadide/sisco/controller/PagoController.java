package pe.com.fadide.sisco.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.com.fadide.sisco.common.controller.CrudController;
import pe.com.fadide.sisco.dto.PagoRequestDTO;
import pe.com.fadide.sisco.dto.PagoResponseDTO;
import pe.com.fadide.sisco.service.PagoService;

import java.util.List;

// CRUD parcial: el backend solo permite consultar y registrar pagos, por eso no hereda de CrudController
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @GetMapping
    public ResponseEntity<List<PagoResponseDTO>> listAll() {
        return ResponseEntity.ok(pagoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.buscarPorId(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PostMapping
    public ResponseEntity<PagoResponseDTO> create(@Valid @RequestBody PagoRequestDTO dto) {
        PagoResponseDTO creado = pagoService.crear(dto);
        return CrudController.creado(creado, creado.getIdPago());
    }
}
