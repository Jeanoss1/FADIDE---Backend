package pe.com.fadide.sisco.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    @NotBlank(message = "El nombre del contratista es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @NotBlank(message = "El RUC es obligatorio")
    @Pattern(regexp = "\\d{11}", message = "El RUC debe tener exactamente 11 dígitos")
    @Column(name = "ruc", nullable = false, unique = true, length = 11)
    private String ruc;
}