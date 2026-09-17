package pe.com.fadide.sisco.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "supervisiones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supervision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_supervision")
    private Long idSupervision;

    @Column(name = "tipo_autor", nullable = false, length = 50)
    private String tipoAutor;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    @Column(name = "avance_porcentual", nullable = false, precision = 5, scale = 2)
    private BigDecimal avancePorcentual;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "evidencia_url", length = 255)
    private String evidenciaUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contrato", nullable = false)
    private Contrato contrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
}
