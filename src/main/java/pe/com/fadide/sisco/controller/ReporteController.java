package pe.com.fadide.sisco.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pe.com.fadide.sisco.config.BadRequestException;
import pe.com.fadide.sisco.dto.FiltroBusqueda;
import pe.com.fadide.sisco.dto.OpcionResponse;
import pe.com.fadide.sisco.dto.TablaReporte;
import pe.com.fadide.sisco.service.ExportadorReporte;
import pe.com.fadide.sisco.service.ReporteService;

/**
 * Reportes con filtros (fecha, estado, texto, obra, contrato, supervisor) y exportación.
 * GET /api/reportes/{tipo}                      -> vista previa en JSON
 * GET /api/reportes/{tipo}/exportar?formato=pdf -> archivo PDF (o formato=excel -> .xlsx)
 */
@RestController
@RequestMapping("/api/reportes")
@PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'SUPERVISOR')")
public class ReporteController {

    // Tipos de reporte que puede generar cada rol (el ADMIN, todos)
    private static final Map<String, Set<String>> REPORTES_POR_ROL = Map.of(
            "ROLE_ADMIN", Set.of("contratos", "pagos", "penalidades", "supervisiones", "licencias", "obras"),
            "ROLE_GESTOR", Set.of("contratos", "pagos", "penalidades", "licencias", "obras"),
            "ROLE_SUPERVISOR", Set.of("supervisiones", "obras"));

    private final ReporteService reporteService;
    private final Map<String, ExportadorReporte> exportadores;

    // Spring inyecta todas las estrategias de exportación disponibles (PDF, Excel)
    public ReporteController(ReporteService reporteService, List<ExportadorReporte> exportadores) {
        this.reporteService = reporteService;
        this.exportadores = exportadores.stream()
                .collect(Collectors.toMap(ExportadorReporte::formato, Function.identity()));
    }

    // Lista de supervisores para el filtro por usuario (sin exponer /api/usuarios, que es solo ADMIN)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    @GetMapping("/supervisores")
    public ResponseEntity<List<OpcionResponse>> supervisores() {
        return ResponseEntity.ok(reporteService.supervisores());
    }

    @GetMapping("/{tipo}")
    public ResponseEntity<TablaReporte> vistaPrevia(@PathVariable String tipo, @ParameterObject FiltroBusqueda filtro,
                                                    Authentication authentication) {
        verificarAcceso(tipo, authentication);
        return ResponseEntity.ok(reporteService.generar(tipo, filtro));
    }

    @GetMapping("/{tipo}/exportar")
    public ResponseEntity<byte[]> exportar(@PathVariable String tipo,
                                           @RequestParam(defaultValue = "pdf") String formato,
                                           @ParameterObject FiltroBusqueda filtro,
                                           Authentication authentication) {
        verificarAcceso(tipo, authentication);
        ExportadorReporte exportador = exportadores.get(formato.toLowerCase());
        if (exportador == null) {
            throw new BadRequestException("Formato no válido: " + formato + ". Use pdf o excel");
        }
        byte[] archivo = exportador.exportar(reporteService.generar(tipo, filtro));
        String nombre = "reporte-" + tipo.toLowerCase() + "-" + LocalDate.now() + "." + exportador.extension();
        return ResponseEntity.ok()
                .contentType(exportador.tipoContenido())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nombre).build().toString())
                .body(archivo);
    }

    // 403 si el rol del usuario no tiene permitido ese tipo de reporte
    private void verificarAcceso(String tipo, Authentication authentication) {
        boolean permitido = authentication.getAuthorities().stream()
                .map(a -> REPORTES_POR_ROL.getOrDefault(a.getAuthority(), Set.of()))
                .anyMatch(tipos -> tipos.contains(tipo.toLowerCase()));
        if (!permitido) {
            throw new AccessDeniedException("Su rol no tiene acceso al reporte '" + tipo + "'");
        }
    }
}
