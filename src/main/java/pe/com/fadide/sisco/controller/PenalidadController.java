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

import pe.com.fadide.sisco.dto.FiltroBusqueda;
import pe.com.fadide.sisco.dto.PageResponse;
import pe.com.fadide.sisco.dto.PenalidadRequestDTO;
import pe.com.fadide.sisco.dto.PenalidadResponseDTO;
import pe.com.fadide.sisco.service.PenalidadService;

@RestController
@RequestMapping("/api/penalidades")
@RequiredArgsConstructor
public class PenalidadController {

    private final PenalidadService penalidadService;

    // Listado paginado con búsqueda y filtros: /page?q=&estado=&desde=&hasta=&page=0&size=10&sort=fechaAplicacion,desc
    @GetMapping("/page")
    public ResponseEntity<PageResponse<PenalidadResponseDTO>> paginar(
            @ParameterObject FiltroBusqueda filtro,
            @ParameterObject @PageableDefault(size = 10, sort = "fechaAplicacion", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(penalidadService.buscar(filtro, pageable));
    }

    @GetMapping
    public ResponseEntity<List<PenalidadResponseDTO>> listAll() {
        return ResponseEntity.ok(penalidadService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PenalidadResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(penalidadService.findById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PostMapping
    public ResponseEntity<PenalidadResponseDTO> create(@Valid @RequestBody PenalidadRequestDTO dto) {
        return new ResponseEntity<>(penalidadService.create(dto), HttpStatus.CREATED);
    }
}