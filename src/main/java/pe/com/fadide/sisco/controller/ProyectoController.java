package pe.com.fadide.sisco.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pe.com.fadide.sisco.common.controller.CrudController;
import pe.com.fadide.sisco.common.service.CrudService;
import pe.com.fadide.sisco.dto.ProyectoRequest;
import pe.com.fadide.sisco.dto.ProyectoResponse;
import pe.com.fadide.sisco.model.EstadoProyecto;
import pe.com.fadide.sisco.service.ProyectoService;

@RestController
@RequestMapping("/api/proyectos")
public class ProyectoController extends CrudController<ProyectoRequest, ProyectoResponse, Long> {

    private final ProyectoService proyectoService;

    public ProyectoController(ProyectoService proyectoService) {
        this.proyectoService = proyectoService;
    }

    // GET /api/proyectos?estado=...[&idCliente=...] (sin filtros responde el GET genérico)
    @GetMapping(params = "estado")
    public ResponseEntity<List<ProyectoResponse>> listarPorEstado(@RequestParam(required = false) EstadoProyecto estado,
                                                                  @RequestParam(required = false) Long idCliente) {
        return ResponseEntity.ok(proyectoService.listar(estado, idCliente));
    }

    // GET /api/proyectos?idCliente=... (sin estado)
    @GetMapping(params = { "idCliente", "!estado" })
    public ResponseEntity<List<ProyectoResponse>> listarPorCliente(@RequestParam(required = false) Long idCliente) {
        return ResponseEntity.ok(proyectoService.listar(null, idCliente));
    }

    @Override
    protected CrudService<ProyectoRequest, ProyectoResponse, Long> getService() {
        return proyectoService;
    }

    @Override
    protected Long obtenerId(ProyectoResponse response) {
        return response.idProyecto();
    }

    @Override
    public String[] rolesEscritura() {
        return new String[] { "ADMIN", "GESTOR" };
    }
}
