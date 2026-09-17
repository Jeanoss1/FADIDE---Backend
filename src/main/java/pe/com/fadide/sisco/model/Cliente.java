package pe.com.fadide.sisco.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long idCliente;

    @Column(name = "nombre_o_razon_social", nullable = false, length = 150)
    private String nombreORazonSocial;

    @Column(name = "tipo_documento", nullable = false, length = 20)
    private String tipoDocumento; // DNI, RUC

    @Column(name = "numero_documento", nullable = false, unique = true, length = 20)
    private String numeroDocumento;

    @Column(name = "direccion", length = 200)
    private String direccion;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "correo", length = 100)
    private String correo;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;
}