package pe.com.fadide.sisco.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contratistas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contratista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contratista")
    private Long idContratista;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(name = "ruc", nullable = false, unique = true, length = 20)
    private String ruc;

    @Column(name = "representante_legal", length = 150)
    private String representanteLegal;

    @Column(name = "dni_representante", length = 15)
    private String dniRepresentante;

    @Column(name = "direccion", length = 200)
    private String direccion;

    @Column(name = "correo", length = 100)
    private String correo;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;
}