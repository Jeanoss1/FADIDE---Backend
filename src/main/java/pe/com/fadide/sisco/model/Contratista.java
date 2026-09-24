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

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "ruc", nullable = false, unique = true, length = 11)
    private String ruc;
}