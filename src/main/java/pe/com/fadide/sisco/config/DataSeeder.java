package pe.com.fadide.sisco.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pe.com.fadide.sisco.model.Rol;
import pe.com.fadide.sisco.model.Usuario;
import pe.com.fadide.sisco.repository.RolRepository;
import pe.com.fadide.sisco.repository.UsuarioRepository;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${sisco.admin.correo}")
    private String adminCorreo;

    @Value("${sisco.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        Rol admin = rolRepository.findByNombre("ADMIN")
                .orElseGet(() -> rolRepository.save(Rol.builder().nombre("ADMIN").build()));
        rolRepository.findByNombre("GESTOR")
                .orElseGet(() -> rolRepository.save(Rol.builder().nombre("GESTOR").build()));
        rolRepository.findByNombre("SUPERVISOR")
                .orElseGet(() -> rolRepository.save(Rol.builder().nombre("SUPERVISOR").build()));

        if (usuarioRepository.findByCorreo(adminCorreo).isEmpty()) {
            usuarioRepository.save(Usuario.builder()
                    .nombreCompleto("Administrador SISCO")
                    .correo(adminCorreo)
                    .documento("00000000")
                    .password(passwordEncoder.encode(adminPassword))
                    .activo(true)
                    .rol(admin)
                    .build());
        }
    }
}
