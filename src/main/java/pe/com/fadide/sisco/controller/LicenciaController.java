package pe.com.fadide.sisco.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pe.com.fadide.sisco.common.controller.CrudController;
import pe.com.fadide.sisco.common.service.CrudService;
import pe.com.fadide.sisco.dto.LicenciaRequest;
import pe.com.fadide.sisco.dto.LicenciaResponse;
import pe.com.fadide.sisco.service.LicenciaService;

@RestController
@RequestMapping("/api/licencias")
public class LicenciaController extends CrudController<LicenciaRequest, LicenciaResponse, Long> {

    private final LicenciaService licenciaService;

    public LicenciaController(LicenciaService licenciaService) {
        this.licenciaService = licenciaService;
    }

    @GetMapping("/por-vencer")
    public ResponseEntity<List<LicenciaResponse>> listarPorVencer(@RequestParam(defaultValue = "30") int dias) {
        return ResponseEntity.ok(licenciaService.listarPorVencer(dias));
    }

    @GetMapping("/proyecto/{idProyecto}")
    public ResponseEntity<LicenciaResponse> obtenerPorProyecto(@PathVariable Long idProyecto) {
        return ResponseEntity.ok(licenciaService.obtenerPorProyecto(idProyecto));
    }

    @Override
    protected CrudService<LicenciaRequest, LicenciaResponse, Long> getService() {
        return licenciaService;
    }

    @Override
    protected Long obtenerId(LicenciaResponse response) {
        return response.idLicencia();
    }

    @Override
    public String[] rolesEscritura() {
        return new String[] { "ADMIN", "GESTOR" };
    }
}
