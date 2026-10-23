package pe.com.fadide.sisco.common.service.impl;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import pe.com.fadide.sisco.common.exception.RecursoNoEncontradoException;
import pe.com.fadide.sisco.common.service.CrudService;

// Patrón Template Method: el CRUD se implementa una sola vez y cada módulo solo define
// el mapeo entre entidad y DTOs, y sus reglas de negocio en los hooks de validación
public abstract class CrudServiceImpl<E, REQ, RES, ID> implements CrudService<REQ, RES, ID> {

    protected abstract JpaRepository<E, ID> getRepository();

    protected abstract RES toResponse(E entidad);

    protected abstract E toEntity(REQ request);

    protected abstract void actualizarEntidad(E entidad, REQ request);

    protected abstract String mensajeNoEncontrado(ID id);

    // Hooks: por defecto no hacen nada, cada módulo sobrescribe los que necesita

    protected List<E> buscarTodos() {
        return getRepository().findAll();
    }

    protected void validarCrear(REQ request) {
    }

    protected void validarActualizar(ID id, REQ request) {
    }

    protected void validarEliminar(ID id, E entidad) {
    }

    @Override
    @Transactional(readOnly = true)
    public List<RES> listar() {
        return buscarTodos().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RES buscarPorId(ID id) {
        return toResponse(buscarEntidad(id));
    }

    @Override
    @Transactional
    public RES crear(REQ request) {
        validarCrear(request);
        return toResponse(getRepository().save(toEntity(request)));
    }

    @Override
    @Transactional
    public RES actualizar(ID id, REQ request) {
        E entidad = buscarEntidad(id);
        validarActualizar(id, request);
        actualizarEntidad(entidad, request);
        return toResponse(getRepository().save(entidad));
    }

    @Override
    @Transactional
    public void eliminar(ID id) {
        E entidad = buscarEntidad(id);
        validarEliminar(id, entidad);
        getRepository().delete(entidad);
    }

    protected E buscarEntidad(ID id) {
        return getRepository().findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(mensajeNoEncontrado(id)));
    }
}
