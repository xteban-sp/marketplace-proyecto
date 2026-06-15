package pe.edu.upeu.auth_service.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pe.edu.upeu.auth_service.entity.User;
import pe.edu.upeu.auth_service.repository.UserRepository;

import java.util.Set;

/**
 * Crea un usuario ADMIN inicial al arrancar si todavia no existe ninguno.
 * Sin esto no habria forma de usar los endpoints protegidos con hasRole('ADMIN')
 * (crear categorias, promover SELLER, etc.) en un sistema recien desplegado.
 *
 * Credenciales por variable de entorno; valores por defecto solo para desarrollo.
 */
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.bootstrap.enabled:true}")
    private boolean enabled;

    @Value("${admin.username:admin}")
    private String adminUsername;

    @Value("${admin.password:Admin12345!}")
    private String adminPassword;

    @Value("${admin.email:admin@marketplace.local}")
    private String adminEmail;

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        if (userRepository.existsByUsername(adminUsername)) {
            log.info("AdminBootstrap: el usuario ADMIN '{}' ya existe, no se crea de nuevo.", adminUsername);
            return;
        }

        User admin = User.builder()
                .fullName("Administrador del Sistema")
                .dni("00000000")
                .email(adminEmail)
                .universityCode("ADMIN0001")
                .phone("000000000")
                .username(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .roles(Set.of("ADMIN", "USER"))
                .enabled(true)
                .build();

        userRepository.save(admin);
        log.warn("AdminBootstrap: usuario ADMIN '{}' creado. CAMBIA la contrasena por defecto en produccion.", adminUsername);
    }
}
