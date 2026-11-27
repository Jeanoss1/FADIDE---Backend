package pe.com.fadide.sisco.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Documento de sustento de una penalidad (acta, informe, carta notarial, foto, hoja de cálculo...).
 * Una penalidad puede tener varias evidencias; el archivo se guarda en la carpeta de evidencias
 * del servidor y aquí solo su nombre aleatorio, tipo y tamaño.
 */
@Entity
@Table(name = "evidencias_penalidad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvidenciaPenalidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evidencia_penalidad")
    private Long idEvidenciaPenalidad;

    @Column(name = "nombre_original", nullable = false, length = 255)
    private String nombreOriginal;

    @Column(name = "archivo", nullable = false, length = 60)
    private String archivo;

    @Column(name = "tipo_contenido", nullable = false, length = 120)
    private String tipoContenido;

    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_penalidad", nullable = false)
    private Penalidad penalidad;
}
