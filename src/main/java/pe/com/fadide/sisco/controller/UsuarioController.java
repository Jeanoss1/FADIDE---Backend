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
import org.springframework.web.bind.annotation.*;

import pe.com.fadide.sisco.dto.FiltroBusqueda;
import pe.com.fadide.sisco.dto.PageResponse;
import pe.com.fadide.sisco.dto.UsuarioRequestDTO;
import pe.com.fadide.sisco.dto.UsuarioResponseDTO;
import pe.com.fadide.sisco.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // Listado paginado con búsqueda y filtros: /page?q=&estado=&desde=&hasta=&page=0&size=10&sort=nombreCompleto,asc
    @GetMapping("/page")
    public ResponseEntity<PageResponse<UsuarioResponseDTO>> paginar(
            @ParameterObject FiltroBusqueda filtro,
            @ParameterObject @PageableDefault(size = 10, sort = "nombreCompleto", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(usuarioService.buscar(filtro, pageable));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listAll() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> create(@Valid @RequestBody UsuarioRequestDTO dto) {
        return new ResponseEntity<>(usuarioService.create(dto), HttpStatus.CREATED);
    }
}
