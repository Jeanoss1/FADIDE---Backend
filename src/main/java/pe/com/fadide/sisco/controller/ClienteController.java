package pe.com.fadide.sisco.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pe.com.fadide.sisco.common.controller.CrudController;
import pe.com.fadide.sisco.common.service.CrudService;
import pe.com.fadide.sisco.dto.ClienteRequest;
import pe.com.fadide.sisco.dto.ClienteResponse;
import pe.com.fadide.sisco.service.ClienteService;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController extends CrudController<ClienteRequest, ClienteResponse, Long> {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // GET /api/clientes?razonSocial=... (sin el parámetro responde el GET genérico)
    @GetMapping(params = "razonSocial")
    public ResponseEntity<List<ClienteResponse>> listarPorRazonSocial(@RequestParam String razonSocial) {
        return ResponseEntity.ok(clienteService.listar(razonSocial));
    }

    @Override
    protected CrudService<ClienteRequest, ClienteResponse, Long> getService() {
        return clienteService;
    }

    @Override
    protected Long obtenerId(ClienteResponse response) {
        return response.idCliente();
    }

    @Override
    public String[] rolesEscritura() {
        return new String[] { "ADMIN", "GESTOR" };
    }
}
