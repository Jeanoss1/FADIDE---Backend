package pe.com.fadide.sisco.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.com.fadide.sisco.config.ResourceNotFoundException;
import pe.com.fadide.sisco.dto.DashboardResponse.Grafico;
import pe.com.fadide.sisco.dto.DashboardResponse.Kpi;
import pe.com.fadide.sisco.dto.DashboardResponse.Punto;
import pe.com.fadide.sisco.dto.DashboardResponse;
import pe.com.fadide.sisco.dto.LicenciaResponse;
import pe.com.fadide.sisco.model.EstadoProyecto;
import pe.com.fadide.sisco.model.Pago;
import pe.com.fadide.sisco.model.Supervision;
import pe.com.fadide.sisco.model.Usuario;
import pe.com.fadide.sisco.repository.ClienteRepository;
import pe.com.fadide.sisco.repository.ContratoRepository;
import pe.com.fadide.sisco.repository.LicenciaRepository;
import pe.com.fadide.sisco.repository.PagoRepository;
import pe.com.fadide.sisco.repository.PenalidadRepository;
import pe.com.fadide.sisco.repository.ProyectoRepository;
import pe.com.fadide.sisco.repository.SupervisionRepository;
import pe.com.fadide.sisco.repository.UsuarioRepository;
import pe.com.fadide.sisco.service.DashboardService;

/**
 * Calcula los indicadores del panel principal con consultas agregadas (count, sum, avg,
 * group by) para no traer todas las filas al servidor. Cada rol recibe un conjunto distinto:
 * <ul>
 *   <li>ADMIN: visión global de la empresa, incluidos los usuarios activos.</li>
 *   <li>GESTOR: visión contractual y financiera (montos, saldos y penalidades).</li>
 *   <li>SUPERVISOR: su trabajo de campo (supervisiones propias y avance de las obras).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final int DIAS_ALERTA_LICENCIA = 30;
    private static final int MESES_HISTORICO = 6;
    private static final Locale ES_PE = Locale.forLanguageTag("es-PE");

    private final ClienteRepository clienteRepository;
    private final ProyectoRepository proyectoRepository;
    private final LicenciaRepository licenciaRepository;
    private final ContratoRepository contratoRepository;
    private final PagoRepository pagoRepository;
    private final PenalidadRepository penalidadRepository;
    private final SupervisionRepository supervisionRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public DashboardResponse obtener(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + correo));
        String rol = usuario.getRol().getNombre();

        // Las licencias son del módulo de gestión: el supervisor no las ve (ni KPI ni listado)
        LocalDate hoy = LocalDate.now();
        List<LicenciaResponse> porVencer = "SUPERVISOR".equals(rol) ? List.of() : licenciaRepository
                .buscarPorVencer(hoy, hoy.plusDays(DIAS_ALERTA_LICENCIA)).stream()
                .map(LicenciaResponse::from)
                .toList();

        List<Kpi> kpis = switch (rol) {
            case "SUPERVISOR" -> kpisSupervisor(correo);
            case "GESTOR" -> kpisGestor(porVencer.size());
            default -> kpisAdmin(porVencer.size());
        };
        List<Grafico> graficos = "SUPERVISOR".equals(rol)
                ? List.of(graficoAvancePorContrato(), graficoSupervisionesPorMes())
                : List.of(graficoPagosPorMes(), graficoObrasPorEstado(), graficoContratosPorEstado(), graficoAvancePorContrato());

        return new DashboardResponse(rol, kpis, graficos, porVencer);
    }

    // ---------- Indicadores por rol ----------

    private List<Kpi> kpisAdmin(int licenciasPorVencer) {
        List<Kpi> kpis = new ArrayList<>(kpisFinancieros());
        kpis.add(0, kpi("clientes", "Clientes", clienteRepository.count(), "NUMERO", "groups", "/pages/cliente"));
        kpis.add(1, kpi("obras", "Obras en ejecución", proyectoRepository.countByEstado(EstadoProyecto.EN_EJECUCION), "NUMERO", "apartment", "/pages/proyecto"));
        kpis.add(kpi("licencias", "Licencias por vencer (30 d)", licenciasPorVencer, "NUMERO", "warning_amber", "/pages/licencia"));
        kpis.add(kpi("usuarios", "Usuarios activos", usuarioRepository.countByActivoTrue(), "NUMERO", "manage_accounts", "/pages/usuario"));
        return kpis;
    }

    private List<Kpi> kpisGestor(int licenciasPorVencer) {
        List<Kpi> kpis = new ArrayList<>(kpisFinancieros());
        kpis.add(0, kpi("obras", "Obras en ejecución", proyectoRepository.countByEstado(EstadoProyecto.EN_EJECUCION), "NUMERO", "apartment", "/pages/proyecto"));
        kpis.add(new Kpi("penalidades", "Penalidades aplicadas", penalidadRepository.sumarUitAplicadas(), "UIT", "gavel", "/pages/penalidad"));
        kpis.add(kpi("licencias", "Licencias por vencer (30 d)", licenciasPorVencer, "NUMERO", "warning_amber", "/pages/licencia"));
        return kpis;
    }

    // Contratos abiertos, monto contratado, monto pagado y saldo pendiente
    private List<Kpi> kpisFinancieros() {
        BigDecimal contratado = contratoRepository.sumarMontoTotal();
        BigDecimal pagado = pagoRepository.sumarPagado();
        return List.of(
                kpi("contratos", "Contratos abiertos", contratoRepository.contarAbiertos(), "NUMERO", "assignment", "/pages/contrato"),
                new Kpi("contratado", "Monto contratado", contratado, "MONEDA", "account_balance", "/pages/contrato"),
                new Kpi("pagado", "Monto pagado", pagado, "MONEDA", "payments", "/pages/pago"),
                new Kpi("saldo", "Saldo por pagar", contratado.subtract(pagado), "MONEDA", "savings", "/pages/reporte"));
    }

    private List<Kpi> kpisSupervisor(String correo) {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        return List.of(
                kpi("misSupervisiones", "Mis supervisiones", supervisionRepository.countByUsuario_Correo(correo), "NUMERO", "fact_check", "/pages/supervision"),
                kpi("supervisionesMes", "Supervisiones del equipo (mes)", supervisionRepository.contarDesde(inicioMes), "NUMERO", "event_available", "/pages/supervision"),
                new Kpi("avance", "Avance físico promedio", escala(BigDecimal.valueOf(supervisionRepository.promedioAvance())), "PORCENTAJE", "trending_up", "/pages/supervision"),
                kpi("obras", "Obras en ejecución", proyectoRepository.countByEstado(EstadoProyecto.EN_EJECUCION), "NUMERO", "apartment", "/pages/proyecto"));
    }

    // ---------- Gráficos ----------

    private Grafico graficoPagosPorMes() {
        Map<YearMonth, BigDecimal> porMes = mesesVacios();
        LocalDate desde = porMes.keySet().iterator().next().atDay(1);
        for (Pago p : pagoRepository.buscarDesde(desde)) {
            porMes.computeIfPresent(YearMonth.from(p.getFechaPago()), (k, v) -> v.add(p.getMonto()));
        }
        return new Grafico("pagosPorMes", "Pagos de los últimos 6 meses", "COLUMNAS", "MONEDA", aPuntos(porMes));
    }

    private Grafico graficoSupervisionesPorMes() {
        Map<YearMonth, BigDecimal> porMes = mesesVacios();
        LocalDate desde = porMes.keySet().iterator().next().atDay(1);
        for (Supervision s : supervisionRepository.buscarDesde(desde)) {
            porMes.computeIfPresent(YearMonth.from(s.getFechaRegistro()), (k, v) -> v.add(BigDecimal.ONE));
        }
        return new Grafico("supervisionesPorMes", "Supervisiones de los últimos 6 meses", "COLUMNAS", "NUMERO", aPuntos(porMes));
    }

    private Grafico graficoObrasPorEstado() {
        List<Punto> datos = proyectoRepository.contarPorEstado().stream()
                .map(r -> new Punto(etiqueta(r[0].toString()), BigDecimal.valueOf((Long) r[1])))
                .toList();
        return new Grafico("obrasPorEstado", "Obras por estado", "BARRAS", "NUMERO", datos);
    }

    private Grafico graficoContratosPorEstado() {
        List<Punto> datos = contratoRepository.contarPorEstado().stream()
                .map(r -> new Punto(etiqueta(r[0].toString()), BigDecimal.valueOf((Long) r[1])))
                .toList();
        return new Grafico("contratosPorEstado", "Contratos por estado", "BARRAS", "NUMERO", datos);
    }

    // Avance físico de la supervisión registrada en cada contrato (las 8 más recientes)
    private Grafico graficoAvancePorContrato() {
        List<Punto> datos = supervisionRepository.listarTodas().stream()
                .sorted(Comparator.comparing(Supervision::getFechaRegistro).reversed())
                .limit(8)
                .map(s -> new Punto(s.getContrato().getNumeroContrato(), escala(s.getAvancePorcentual())))
                .toList();
        return new Grafico("avancePorContrato", "Avance físico por contrato", "MEDIDORES", "PORCENTAJE", datos);
    }

    // ---------- utilitarios ----------

    private static Kpi kpi(String clave, String etiqueta, long valor, String formato, String icono, String ruta) {
        return new Kpi(clave, etiqueta, BigDecimal.valueOf(valor), formato, icono, ruta);
    }

    // Últimos 6 meses (incluido el actual) en orden cronológico, todos en cero
    private static Map<YearMonth, BigDecimal> mesesVacios() {
        Map<YearMonth, BigDecimal> meses = new LinkedHashMap<>();
        YearMonth actual = YearMonth.now();
        for (int i = MESES_HISTORICO - 1; i >= 0; i--) {
            meses.put(actual.minusMonths(i), BigDecimal.ZERO);
        }
        return meses;
    }

    private static List<Punto> aPuntos(Map<YearMonth, BigDecimal> porMes) {
        return porMes.entrySet().stream()
                .map(e -> new Punto(
                        e.getKey().getMonth().getDisplayName(TextStyle.SHORT, ES_PE) + " " + e.getKey().getYear(),
                        e.getValue()))
                .toList();
    }

    // EN_EJECUCION -> En ejecución
    private static String etiqueta(String estado) {
        String texto = estado.replace('_', ' ').toLowerCase(ES_PE);
        texto = texto.replace("ejecucion", "ejecución");
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    private static BigDecimal escala(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor.setScale(2, RoundingMode.HALF_UP);
    }
}
