package pe.com.fadide.sisco.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "penalidades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Penalidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_penalidad")
    private Long idPenalidad;

    @NotNull(message = "El monto en UIT es obligatorio")
    @Positive(message = "El monto en UIT debe ser mayor que cero")
    @Digits(integer = 6, fraction = 4, message = "El monto en UIT admite hasta 6 enteros y 4 decimales")
    @Column(name = "monto_uit", nullable = false, precision = 10, scale = 4)
    private BigDecimal montoUit;

    @NotNull(message = "La fecha de aplicación es obligatoria")
    @Column(name = "fecha_aplicacion", nullable = false)
    private LocalDate fechaAplicacion;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 255, message = "El motivo no puede superar 255 caracteres")
    @Column(name = "motivo", nullable = false, length = 255)
    private String motivo;

    @NotBlank(message = "El estado es obligatorio")
    @Size(max = 30, message = "El estado no puede superar 30 caracteres")
    @Column(name = "estado", nullable = false, length = 30)
    private String estado; // APLICADA, CONDONADA, PAGADA

    @NotNull(message = "La penalidad debe pertenecer a un contrato")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contrato", nullable = false)
    private Contrato contrato;
}
