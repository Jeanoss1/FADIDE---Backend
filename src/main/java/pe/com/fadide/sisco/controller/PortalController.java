package pe.com.fadide.sisco.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.com.fadide.sisco.dto.MiObraResponse;
import pe.com.fadide.sisco.service.PortalService;

import java.util.List;

// Portal de autoconsulta del cliente (rol CLIENTE): solo ve sus propias obras
@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
public class PortalController {

    private final PortalService portalService;

    @GetMapping("/mis-obras")
    public ResponseEntity<List<MiObraResponse>> misObras(Authentication authentication) {
        return ResponseEntity.ok(portalService.misObras(authentication.getName()));
    }
}
