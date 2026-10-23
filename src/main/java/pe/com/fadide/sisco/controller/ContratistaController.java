package pe.com.fadide.sisco.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pe.com.fadide.sisco.common.controller.CrudController;
import pe.com.fadide.sisco.common.service.CrudService;
import pe.com.fadide.sisco.dto.ContratistaRequest;
import pe.com.fadide.sisco.dto.ContratistaResponse;
import pe.com.fadide.sisco.service.ContratistaService;

@RestController
@RequestMapping("/api/contratistas")
public class ContratistaController extends CrudController<ContratistaRequest, ContratistaResponse, Long> {

    private final ContratistaService contratistaService;

    public ContratistaController(ContratistaService contratistaService) {
        this.contratistaService = contratistaService;
    }

    // GET /api/contratistas?nombre=... (sin el parámetro responde el GET genérico)
    @GetMapping(params = "nombre")
    public ResponseEntity<List<ContratistaResponse>> listarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(contratistaService.listar(nombre));
    }

    @Override
    protected CrudService<ContratistaRequest, ContratistaResponse, Long> getService() {
        return contratistaService;
    }

    @Override
    protected Long obtenerId(ContratistaResponse response) {
        return response.idContratista();
    }

    @Override
    public String[] rolesEscritura() {
        return new String[] { "ADMIN", "GESTOR" };
    }
}
