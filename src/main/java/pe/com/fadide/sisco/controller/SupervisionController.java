package pe.com.fadide.sisco.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.com.fadide.sisco.dto.FiltroBusqueda;
import pe.com.fadide.sisco.dto.PageResponse;
import pe.com.fadide.sisco.dto.SupervisionRequest;
import pe.com.fadide.sisco.dto.SupervisionResponse;
import pe.com.fadide.sisco.service.SupervisionService;

@RestController
@RequestMapping("/api/supervisiones")
public class SupervisionController {

    private final SupervisionService supervisionService;

    public SupervisionController(SupervisionService supervisionService) {
        this.supervisionService = supervisionService;
    }

    // Listado paginado con búsqueda y filtros: /page?q=&estado=&desde=&hasta=&page=0&size=10&sort=fechaRegistro,desc
    @GetMapping("/page")
    public ResponseEntity<PageResponse<SupervisionResponse>> paginar(
            @ParameterObject FiltroBusqueda filtro,
            @ParameterObject @PageableDefault(size = 10, sort = "fechaRegistro", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(supervisionService.buscar(filtro, pageable));
    }

    @GetMapping
    public ResponseEntity<List<SupervisionResponse>> listar() {
        return ResponseEntity.ok(supervisionService.listar());
    }

    @GetMapping("/contrato/{idContrato}")
    public ResponseEntity<SupervisionResponse> obtenerPorContrato(@PathVariable Long idContrato) {
        return ResponseEntity.ok(supervisionService.obtenerPorContrato(idContrato));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupervisionResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(supervisionService.obtener(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    @PostMapping
    public ResponseEntity<SupervisionResponse> crear(@Valid @RequestBody SupervisionRequest request,
                                                      Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supervisionService.crear(request, authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    @PutMapping("/{id}")
    public ResponseEntity<SupervisionResponse> actualizar(@PathVariable Long id,
                                                          @Valid @RequestBody SupervisionRequest request) {
        return ResponseEntity.ok(supervisionService.actualizar(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        supervisionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
