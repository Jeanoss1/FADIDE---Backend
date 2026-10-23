package pe.com.fadide.sisco.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pe.com.fadide.sisco.common.controller.CrudController;
import pe.com.fadide.sisco.common.service.CrudService;
import pe.com.fadide.sisco.dto.ObservacionRequest;
import pe.com.fadide.sisco.dto.ObservacionResponse;
import pe.com.fadide.sisco.service.ObservacionService;

@RestController
@RequestMapping("/api/observaciones")
public class ObservacionController extends CrudController<ObservacionRequest, ObservacionResponse, Long> {

    private final ObservacionService observacionService;

    public ObservacionController(ObservacionService observacionService) {
        this.observacionService = observacionService;
    }

    // GET /api/observaciones?idSupervision=... (sin el parámetro responde el GET genérico)
    @GetMapping(params = "idSupervision")
    public ResponseEntity<List<ObservacionResponse>> listarPorSupervision(
            @RequestParam(required = false) Long idSupervision) {
        return ResponseEntity.ok(observacionService.listar(idSupervision));
    }

    @Override
    protected CrudService<ObservacionRequest, ObservacionResponse, Long> getService() {
        return observacionService;
    }

    @Override
    protected Long obtenerId(ObservacionResponse response) {
        return response.idObservacion();
    }

    @Override
    public String[] rolesEscritura() {
        return new String[] { "SUPERVISOR" };
    }
}
