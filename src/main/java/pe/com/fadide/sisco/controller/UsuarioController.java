package pe.com.fadide.sisco.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.com.fadide.sisco.common.controller.CrudController;
import pe.com.fadide.sisco.dto.UsuarioRequestDTO;
import pe.com.fadide.sisco.dto.UsuarioResponseDTO;
import pe.com.fadide.sisco.service.UsuarioService;

import java.util.List;

// CRUD parcial: solo se listan y crean usuarios, por eso no hereda de CrudController.
// El acceso a /api/usuarios/** está restringido a ADMIN en SecurityConfig.
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listAll() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> create(@Valid @RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO creado = usuarioService.crear(dto);
        return CrudController.creado(creado, creado.getIdUsuario());
    }
}
