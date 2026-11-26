package pe.com.fadide.sisco.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.com.fadide.sisco.dto.DashboardResponse;
import pe.com.fadide.sisco.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // Indicadores y gráficos del panel principal adaptados al rol del usuario autenticado
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'SUPERVISOR')")
    @GetMapping
    public ResponseEntity<DashboardResponse> obtener(Authentication authentication) {
        return ResponseEntity.ok(dashboardService.obtener(authentication.getName()));
    }
}
