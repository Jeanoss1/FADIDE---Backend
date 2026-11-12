package pe.com.fadide.sisco.common.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.com.fadide.sisco.common.service.GenericService;
import pe.com.fadide.sisco.config.ResourceNotFoundException;

import java.util.List;

public abstract class GenericController<T, ID> {

    protected abstract GenericService<T, ID> getService();

    @GetMapping
    public ResponseEntity<List<T>> listAll() {
        return ResponseEntity.ok(getService().findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<T> getById(@PathVariable ID id) {
        // 404 con cuerpo JSON uniforme (GlobalExceptionHandler) en vez de una respuesta vacía
        return getService().findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado con ID: " + id));
    }

    @PostMapping
    public ResponseEntity<T> create(@Valid @RequestBody T entity) {
        return new ResponseEntity<>(getService().save(entity), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<T> update(@PathVariable ID id, @Valid @RequestBody T entity) {
        return ResponseEntity.ok(getService().update(id, entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable ID id) {
        getService().deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
