package pe.com.fadide.sisco.controller;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import pe.com.fadide.sisco.dto.ContratoRequestDTO;
import pe.com.fadide.sisco.dto.ContratoResponseDTO;
import pe.com.fadide.sisco.dto.FiltroBusqueda;
import pe.com.fadide.sisco.dto.PageResponse;
import pe.com.fadide.sisco.service.ContratoService;

@RestController
@RequestMapping("/api/contratos")
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;

    // Listado paginado con búsqueda y filtros: /page?q=&estado=&desde=&hasta=&page=0&size=10&sort=fechaInicio,desc
    @GetMapping("/page")
    public ResponseEntity<PageResponse<ContratoResponseDTO>> paginar(
            @ParameterObject FiltroBusqueda filtro,
            @ParameterObject @PageableDefault(size = 10, sort = "fechaInicio", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(contratoService.buscar(filtro, pageable));
    }

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