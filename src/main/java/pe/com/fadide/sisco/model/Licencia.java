package pe.com.fadide.sisco.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "licencias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Licencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_licencia")
    private Long idLicencia;

    @Column(name = "numero_resolucion", nullable = false, unique = true, length = 50)
    private String numeroResolucion;

    @Column(name = "fecha_aprobacion", nullable = false)
    private LocalDate fechaAprobacion;

    @Column(name = "entidad_emisora", nullable = false, length = 100)
    private String entidadEmisora;
}