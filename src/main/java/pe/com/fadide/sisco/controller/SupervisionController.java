package pe.com.fadide.sisco.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.com.fadide.sisco.common.controller.CrudController;
import pe.com.fadide.sisco.common.service.CrudService;
import pe.com.fadide.sisco.dto.SupervisionRequest;
import pe.com.fadide.sisco.dto.SupervisionResponse;
import pe.com.fadide.sisco.service.SupervisionService;

@RestController
@RequestMapping("/api/supervisiones")
public class SupervisionController extends CrudController<SupervisionRequest, SupervisionResponse, Long> {

    private final SupervisionService supervisionService;

    public SupervisionController(SupervisionService supervisionService) {
        this.supervisionService = supervisionService;
    }

    @GetMapping("/contrato/{idContrato}")
    public ResponseEntity<SupervisionResponse> obtenerPorContrato(@PathVariable Long idContrato) {
        return ResponseEntity.ok(supervisionService.obtenerPorContrato(idContrato));
    }

    @Override
    protected CrudService<SupervisionRequest, SupervisionResponse, Long> getService() {
        return supervisionService;
    }

    @Override
    protected Long obtenerId(SupervisionResponse response) {
        return response.idSupervision();
    }

    // Solo el supervisor registra y modifica la bitácora
    @Override
    public String[] rolesEscritura() {
        return new String[] { "SUPERVISOR" };
    }
}
