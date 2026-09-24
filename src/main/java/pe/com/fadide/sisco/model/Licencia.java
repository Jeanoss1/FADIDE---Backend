package pe.com.fadide.sisco.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(name = "numero", nullable = false, unique = true, length = 50)
    private String numero;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proyecto", nullable = false, unique = true)
    private Proyecto proyecto;
}
