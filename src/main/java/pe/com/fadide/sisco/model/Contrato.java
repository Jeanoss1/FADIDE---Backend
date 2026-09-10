package pe.com.fadide.sisco.model;

import jakarta.persistence.*;
import lombok.*;

public class Contrato {
    @Entity
    @Table(name = "roles")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Rol {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id_rol")
        private Long idRol;

        @Column(name = "nombre", nullable = false, unique = true, length = 50)
        private String nombre;
    }

    @Entity
    @Table(name = "usuarios")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Usuario {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id_usuario")
        private Long idUsuario;

        @Column(name = "nombre", nullable = false, length = 100)
        private String nombre;

        @Column(name = "correo", nullable = false, unique = true, length = 100)
        private String correo;

        @Column(name = "password", nullable = false, length = 255)
        private String password;

        @Column(name = "estado", nullable = false)
        @Builder.Default
        private Boolean estado = true;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "id_rol", nullable = false)
        private Rol rol;
    }
}
