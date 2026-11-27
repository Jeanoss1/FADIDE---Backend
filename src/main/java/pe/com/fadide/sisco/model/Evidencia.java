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

@Entity
@Table(name = "evidencias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evidencia")
    private Long idEvidencia;

    // Enlace (URL) de la evidencia o, si se subió un archivo, su nombre original
    @Column(name = "imagen", nullable = false, length = 500)
    private String imagen;

    // Archivo subido: nombre con el que se guardó en el servidor (UUID), tipo y tamaño.
    // Son nulos en las evidencias registradas solo con un enlace.
    @Column(name = "archivo", length = 60)
    private String archivo;

    @Column(name = "tipo_contenido", length = 120)
    private String tipoContenido;

    @Column(name = "tamano_bytes")
    private Long tamanoBytes;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_supervision", nullable = false)
    private Supervision supervision;
}
