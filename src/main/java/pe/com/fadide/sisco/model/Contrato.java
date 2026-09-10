package pe.com.fadide.sisco.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contratos")
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_contrato", nullable = false, unique = true, length = 50)
    private String codigoContrato; // Ej: "CONT-2026-001"

    @Column(name = "nombre_obra", nullable = false, length = 250)
    private String nombreObra; // Ej: "Supervisión de Pavimentación Vial Tramo 1"

    @Column(name = "monto_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal; // Para valores monetarios en supervisión/obras

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado; // Ej: "ACTIVO", "SUSPENDIDO", "LIQUIDADO"

    // Constructor vacío obligatorio para JPA
    public Contrato() {
    }

    // Constructor completo
    public Contrato(Long id, String codigoContrato, String nombreObra, BigDecimal montoTotal, LocalDate fechaInicio, LocalDate fechaFin, String estado) {
        this.id = id;
        this.codigoContrato = codigoContrato;
        this.nombreObra = nombreObra;
        this.montoTotal = montoTotal;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoContrato() {
        return codigoContrato;
    }

    public void setCodigoContrato(String codigoContrato) {
        this.codigoContrato = codigoContrato;
    }

    public String getNombreObra() {
        return nombreObra;
    }

    public void setNombreObra(String nombreObra) {
        this.nombreObra = nombreObra;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}