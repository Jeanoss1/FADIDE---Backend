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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pe.com.fadide.sisco.dto.ClienteRequest;
import pe.com.fadide.sisco.dto.ClienteResponse;
import pe.com.fadide.sisco.dto.FiltroBusqueda;
import pe.com.fadide.sisco.dto.PageResponse;
import pe.com.fadide.sisco.service.ClienteService;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // Listado paginado con búsqueda y filtros: /page?q=&estado=&desde=&hasta=&page=0&size=10&sort=razonSocial,asc
    @GetMapping("/page")
    public ResponseEntity<PageResponse<ClienteResponse>> paginar(
            @ParameterObject FiltroBusqueda filtro,
            @ParameterObject @PageableDefault(size = 10, sort = "razonSocial", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(clienteService.buscar(filtro, pageable));
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar(@RequestParam(required = false) String razonSocial) {
        return ResponseEntity.ok(clienteService.listar(razonSocial));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.obtener(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizar(@PathVariable Long id,
                                                      @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(clienteService.actualizar(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
