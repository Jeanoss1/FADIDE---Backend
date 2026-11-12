package pe.com.fadide.sisco.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    // Validación del modelo: Hibernate Validator la ejecuta antes de cada INSERT/UPDATE
    @NotBlank(message = "La razón social es obligatoria")
    @Size(max = 150, message = "La razón social no puede superar 150 caracteres")
    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @NotBlank(message = "El RUC es obligatorio")
    @Pattern(regexp = "\\d{11}", message = "El RUC debe tener exactamente 11 dígitos")
    @Column(name = "ruc", nullable = false, unique = true, length = 11)
    private String ruc;

    @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
    @Column(name = "direccion", length = 200)
    private String direccion;

    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
    @Column(name = "telefono", length = 20)
    private String telefono;
}
