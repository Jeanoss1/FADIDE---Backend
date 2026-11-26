package pe.com.fadide.sisco.service.impl;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.com.fadide.sisco.config.BadRequestException;
import pe.com.fadide.sisco.dto.FiltroBusqueda;
import pe.com.fadide.sisco.dto.OpcionResponse;
import pe.com.fadide.sisco.dto.TablaReporte;
import pe.com.fadide.sisco.model.Contrato;
import pe.com.fadide.sisco.model.Licencia;
import pe.com.fadide.sisco.model.Pago;
import pe.com.fadide.sisco.model.Penalidad;
import pe.com.fadide.sisco.model.Proyecto;
import pe.com.fadide.sisco.model.Supervision;
import pe.com.fadide.sisco.repository.ContratoRepository;
import pe.com.fadide.sisco.repository.LicenciaRepository;
import pe.com.fadide.sisco.repository.PagoRepository;
import pe.com.fadide.sisco.repository.PenalidadRepository;
import pe.com.fadide.sisco.repository.ProyectoRepository;
import pe.com.fadide.sisco.repository.SupervisionRepository;
import pe.com.fadide.sisco.repository.UsuarioRepository;
import pe.com.fadide.sisco.service.ReporteService;
import pe.com.fadide.sisco.specification.Especificaciones;

/**
 * Arma los reportes reutilizando las mismas especificaciones (filtros) de los listados
 * paginados. Los valores salen ya formateados (fechas dd/MM/yyyy, montos en soles) para que
 * la vista previa y los archivos exportados muestren exactamente lo mismo.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale ES_PE = Locale.forLanguageTag("es-PE");
    private static final int DIAS_ALERTA_LICENCIA = 30;

    private final ContratoRepository contratoRepository;
    private final PagoRepository pagoRepository;
    private final PenalidadRepository penalidadRepository;
    private final SupervisionRepository supervisionRepository;
    private final LicenciaRepository licenciaRepository;
    private final ProyectoRepository proyectoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public TablaReporte generar(String tipo, FiltroBusqueda filtro) {
        return switch (tipo.toLowerCase(ES_PE)) {
            case "contratos" -> contratos(filtro);
            case "pagos" -> pagos(filtro);
            case "penalidades" -> penalidades(filtro);
            case "supervisiones" -> supervisiones(filtro);
            case "licencias" -> licencias(filtro);
            case "obras" -> obras(filtro);
            default -> throw new BadRequestException("Tipo de reporte no válido: " + tipo
                    + ". Use contratos, pagos, penalidades, supervisiones, licencias u obras");
        };
    }

    @Override
    public List<OpcionResponse> supervisores() {
        return usuarioRepository.findByRol_NombreOrderByNombreCompleto("SUPERVISOR").stream()
                .map(u -> new OpcionResponse(u.getIdUsuario(), u.getNombreCompleto()))
                .toList();
    }

    // Contratos con lo pagado y el saldo de cada uno (seguimiento de pagos)
    private TablaReporte contratos(FiltroBusqueda f) {
        Map<Long, BigDecimal> pagadoPorContrato = new HashMap<>();
        pagoRepository.sumarPagadoPorContrato()
                .forEach(r -> pagadoPorContrato.put((Long) r[0], (BigDecimal) r[1]));

        List<Contrato> contratos = contratoRepository.findAll(Especificaciones.contratos(f), Sort.by("fechaInicio").descending());
        BigDecimal totalMonto = BigDecimal.ZERO;
        BigDecimal totalPagado = BigDecimal.ZERO;
        List<List<String>> filas = new ArrayList<>();
        for (Contrato c : contratos) {
            BigDecimal pagado = pagadoPorContrato.getOrDefault(c.getIdContrato(), BigDecimal.ZERO);
            totalMonto = totalMonto.add(c.getMontoTotal());
            totalPagado = totalPagado.add(pagado);
            filas.add(List.of(
                    c.getNumeroContrato(),
                    c.getProyecto().getNombre(),
                    c.getProyecto().getCliente().getRazonSocial(),
                    fecha(c.getFechaInicio()),
                    fecha(c.getFechaFin()),
                    etiqueta(c.getEstado()),
                    soles(c.getMontoTotal()),
                    soles(pagado),
                    soles(c.getMontoTotal().subtract(pagado))));
        }
        return new TablaReporte("Reporte de contratos y saldos", describir(f, "Inicio del contrato"),
                List.of("N.° contrato", "Obra", "Cliente", "Inicio", "Fin", "Estado", "Monto", "Pagado", "Saldo"),
                List.of(6, 7, 8), filas,
                List.of("TOTAL", "", "", "", "", "", soles(totalMonto), soles(totalPagado), soles(totalMonto.subtract(totalPagado))));
    }

    private TablaReporte pagos(FiltroBusqueda f) {
        List<Pago> pagos = pagoRepository.findAll(Especificaciones.pagos(f), Sort.by("fechaPago").descending());
        BigDecimal total = BigDecimal.ZERO;
        List<List<String>> filas = new ArrayList<>();
        for (Pago p : pagos) {
            if (!"ANULADO".equalsIgnoreCase(p.getEstado())) {
                total = total.add(p.getMonto());
            }
            filas.add(List.of(
                    fecha(p.getFechaPago()),
                    p.getContrato().getNumeroContrato(),
                    p.getContrato().getProyecto().getNombre(),
                    etiqueta(p.getEstado()),
                    Boolean.TRUE.equals(p.getConformidadEmitida()) ? "Sí" : "No",
                    soles(p.getMonto())));
        }
        return new TablaReporte("Reporte de pagos", describir(f, "Fecha de pago"),
                List.of("Fecha", "N.° contrato", "Obra", "Estado", "Conformidad", "Monto"),
                List.of(5), filas,
                List.of("TOTAL (sin anulados)", "", "", "", "", soles(total)));
    }

    private TablaReporte penalidades(FiltroBusqueda f) {
        List<Penalidad> penalidades = penalidadRepository.findAll(Especificaciones.penalidades(f), Sort.by("fechaAplicacion").descending());
        BigDecimal total = BigDecimal.ZERO;
        List<List<String>> filas = new ArrayList<>();
        for (Penalidad p : penalidades) {
            total = total.add(p.getMontoUit());
            filas.add(List.of(
                    fecha(p.getFechaAplicacion()),
                    p.getContrato().getNumeroContrato(),
                    p.getMotivo(),
                    etiqueta(p.getEstado()),
                    uit(p.getMontoUit())));
        }
        return new TablaReporte("Reporte de penalidades", describir(f, "Fecha de aplicación"),
                List.of("Fecha", "N.° contrato", "Motivo", "Estado", "Monto (UIT)"),
                List.of(4), filas,
                List.of("TOTAL", "", "", "", uit(total)));
    }

    private TablaReporte supervisiones(FiltroBusqueda f) {
        List<Supervision> supervisiones = supervisionRepository.findAll(Especificaciones.supervisiones(f), Sort.by("fechaRegistro").descending());
        List<List<String>> filas = new ArrayList<>();
        for (Supervision s : supervisiones) {
            filas.add(List.of(
                    fecha(s.getFechaRegistro()),
                    s.getContrato().getNumeroContrato(),
                    s.getContrato().getProyecto().getNombre(),
                    s.getUsuario().getNombreCompleto(),
                    s.getAvancePorcentual().stripTrailingZeros().toPlainString() + " %",
                    s.getObservaciones() == null ? "" : s.getObservaciones()));
        }
        return new TablaReporte("Reporte de supervisiones", describir(f, "Fecha de registro"),
                List.of("Fecha", "N.° contrato", "Obra", "Supervisor", "Avance", "Observaciones"),
                List.of(4), filas, null);
    }

    private TablaReporte licencias(FiltroBusqueda f) {
        LocalDate hoy = LocalDate.now();
        List<Licencia> licencias = licenciaRepository.findAll(Especificaciones.licencias(f), Sort.by("fechaVencimiento"));
        List<List<String>> filas = new ArrayList<>();
        for (Licencia l : licencias) {
            long dias = ChronoUnit.DAYS.between(hoy, l.getFechaVencimiento());
            String situacion = dias < 0 ? "Vencida" : dias <= DIAS_ALERTA_LICENCIA ? "Por vencer" : "Vigente";
            filas.add(List.of(
                    l.getNumero(),
                    l.getProyecto().getNombre(),
                    fecha(l.getFechaEmision()),
                    fecha(l.getFechaVencimiento()),
                    String.valueOf(dias),
                    situacion));
        }
        return new TablaReporte("Reporte de licencias", describir(f, "Fecha de vencimiento"),
                List.of("N.° licencia", "Obra", "Emisión", "Vencimiento", "Días restantes", "Situación"),
                List.of(4), filas, null);
    }

    private TablaReporte obras(FiltroBusqueda f) {
        List<Proyecto> obras = proyectoRepository.findAll(Especificaciones.proyectos(f), Sort.by("nombre"));
        List<List<String>> filas = new ArrayList<>();
        for (Proyecto p : obras) {
            filas.add(List.of(
                    p.getNombre(),
                    p.getUbicacion(),
                    p.getCliente().getRazonSocial(),
                    etiqueta(p.getEstado().name()),
                    String.valueOf(p.getContratistas().size())));
        }
        return new TablaReporte("Reporte de obras", describir(f, null),
                List.of("Obra", "Ubicación", "Cliente", "Estado", "Contratistas"),
                List.of(4), filas, null);
    }

    // ---------- utilitarios ----------

    // "Fecha de pago: 01/09/2026 al 30/11/2026 · Estado: Pagado · Búsqueda: "obra""
    private static String describir(FiltroBusqueda f, String campoFecha) {
        List<String> partes = new ArrayList<>();
        if (campoFecha != null && (f.desde() != null || f.hasta() != null)) {
            partes.add(campoFecha + ": " + (f.desde() != null ? fecha(f.desde()) : "inicio")
                    + " al " + (f.hasta() != null ? fecha(f.hasta()) : "hoy"));
        }
        if (f.tieneEstado()) {
            partes.add("Estado: " + etiqueta(f.estado()));
        }
        if (f.tieneTexto()) {
            partes.add("Búsqueda: \"" + f.q().trim() + "\"");
        }
        return partes.isEmpty() ? "Sin filtros: todos los registros" : String.join(" · ", partes);
    }

    private static String fecha(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FECHA);
    }

    private static String soles(BigDecimal monto) {
        NumberFormat formato = NumberFormat.getNumberInstance(Locale.US);
        formato.setMinimumFractionDigits(2);
        formato.setMaximumFractionDigits(2);
        return "S/ " + formato.format(monto == null ? BigDecimal.ZERO : monto);
    }

    private static String uit(BigDecimal monto) {
        return monto.stripTrailingZeros().toPlainString() + " UIT";
    }

    private static String etiqueta(String estado) {
        if (estado == null || estado.isBlank()) {
            return "";
        }
        String texto = estado.trim().replace('_', ' ').toLowerCase(ES_PE);
        texto = texto.replace("ejecucion", "ejecución");
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
