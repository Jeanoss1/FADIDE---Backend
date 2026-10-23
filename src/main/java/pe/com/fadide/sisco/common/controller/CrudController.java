package pe.com.fadide.sisco.common.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import pe.com.fadide.sisco.common.service.CrudService;

// Endpoints CRUD comunes. Cada controller hijo define su @RequestMapping, su service,
// cómo obtener el id de la respuesta y qué roles pueden escribir.
// Si el id no existe, el service lanza RecursoNoEncontradoException y se responde 404.
public abstract class CrudController<REQ, RES, ID> {

    protected abstract CrudService<REQ, RES, ID> getService();

    protected abstract ID obtenerId(RES response);

    // Roles que pueden crear, actualizar y eliminar (los evalúa @PreAuthorize)
    public abstract String[] rolesEscritura();

    @GetMapping
    public ResponseEntity<List<RES>> listar() {
        return ResponseEntity.ok(getService().listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RES> buscarPorId(@PathVariable ID id) {
        return ResponseEntity.ok(getService().buscarPorId(id));
    }

    @PreAuthorize("hasAnyRole(this.rolesEscritura())")
    @PostMapping
    public ResponseEntity<RES> crear(@Valid @RequestBody REQ request) {
        RES creado = getService().crear(request);
        return creado(creado, obtenerId(creado));
    }

    @PreAuthorize("hasAnyRole(this.rolesEscritura())")
    @PutMapping("/{id}")
    public ResponseEntity<RES> actualizar(@PathVariable ID id, @Valid @RequestBody REQ request) {
        return ResponseEntity.ok(getService().actualizar(id, request));
    }

    @PreAuthorize("hasAnyRole(this.rolesEscritura())")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable ID id) {
        getService().eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // 201 Created con la cabecera Location apuntando al nuevo recurso (ej. /api/clientes/5)
    public static <T> ResponseEntity<T> creado(T body, Object id) {
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).body(body);
    }
}
