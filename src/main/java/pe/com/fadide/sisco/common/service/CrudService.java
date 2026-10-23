package pe.com.fadide.sisco.common.service;

import java.util.List;

// CRUD básico común a todos los módulos: recibe REQ (DTO de entrada) y devuelve RES (DTO de salida)
public interface CrudService<REQ, RES, ID> {
    List<RES> listar();
    RES buscarPorId(ID id);
    RES crear(REQ request);
    RES actualizar(ID id, REQ request);
    void eliminar(ID id);
}
