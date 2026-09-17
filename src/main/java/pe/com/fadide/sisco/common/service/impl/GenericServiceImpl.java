package pe.com.fadide.sisco.common.service.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.fadide.sisco.common.service.GenericService;

import java.util.List;
import java.util.Optional;

public abstract class GenericServiceImpl<T, ID> implements GenericService<T, ID> {

    protected abstract JpaRepository<T, ID> getRepository();

    @Override
    public List<T> findAll() {
        return getRepository().findAll();
    }

    @Override
    public Optional<T> findById(ID id) {
        return getRepository().findById(id);
    }

    @Override
    public T save(T entity) {
        return getRepository().save(entity);
    }

    @Override
    public T update(ID id, T entity) {
        if (!getRepository().existsById(id)) {
            throw new RuntimeException("Registro no encontrado con ID: " + id);
        }
        return getRepository().save(entity);
    }

    @Override
    public void deleteById(ID id) {
        if (!getRepository().existsById(id)) {
            throw new RuntimeException("Registro no encontrado con ID: " + id);
        }
        getRepository().deleteById(id);
    }
}