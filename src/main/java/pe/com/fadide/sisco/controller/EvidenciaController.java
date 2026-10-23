package pe.com.fadide.sisco.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pe.com.fadide.sisco.common.controller.CrudController;
import pe.com.fadide.sisco.common.service.CrudService;
import pe.com.fadide.sisco.dto.EvidenciaRequest;
import pe.com.fadide.sisco.dto.EvidenciaResponse;
import pe.com.fadide.sisco.service.EvidenciaService;

@RestController
@RequestMapping("/api/evidencias")
public class EvidenciaController extends CrudController<EvidenciaRequest, EvidenciaResponse, Long> {

    private final EvidenciaService evidenciaService;

    public EvidenciaController(EvidenciaService evidenciaService) {
        this.evidenciaService = evidenciaService;
    }

    // GET /api/evidencias?idSupervision=... (sin el parámetro responde el GET genérico)
    @GetMapping(params = "idSupervision")
    public ResponseEntity<List<EvidenciaResponse>> listarPorSupervision(
            @RequestParam(required = false) Long idSupervision) {
        return ResponseEntity.ok(evidenciaService.listar(idSupervision));
    }

    @Override
    protected CrudService<EvidenciaRequest, EvidenciaResponse, Long> getService() {
        return evidenciaService;
    }

    @Override
    protected Long obtenerId(EvidenciaResponse response) {
        return response.idEvidencia();
    }

    @Override
    public String[] rolesEscritura() {
        return new String[] { "SUPERVISOR" };
    }
}
