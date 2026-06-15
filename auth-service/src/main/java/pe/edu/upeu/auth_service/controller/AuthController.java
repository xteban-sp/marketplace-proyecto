package pe.edu.upeu.auth_service.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.auth_service.dto.LoginRequest;
import pe.edu.upeu.auth_service.dto.RegisterRequest;
import pe.edu.upeu.auth_service.dto.AuthResponse;
import pe.edu.upeu.auth_service.entity.User;
import pe.edu.upeu.auth_service.repository.UserRepository;
import pe.edu.upeu.auth_service.security.JwtUtil;
import pe.edu.upeu.auth_service.service.MailService;
import pe.edu.upeu.auth_service.service.ResilienceService;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping({"/auth", "/api/auth"})
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticación y registro de usuarios")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ResilienceService resilienceService;
    private final MailService mailService;

    // URL base pública (gateway) para armar el enlace de verificación.
    @Value("${app.verify-base-url:http://localhost:8080}")
    private String verifyBaseUrl;

    // A dónde redirigir tras verificar (el frontend).
    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Operation(summary = "Registrar nuevo usuario", description = "Crea cuenta con validación de DNI, email, código universitario y celular")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro exitoso"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o usuario ya existe"),
            @ApiResponse(responseCode = "409", description = "Conflicto: usuario duplicado por race condition")
    })
    @PostMapping("/register")
    @Transactional
    // FIX: @Transactional para atomicidad + manejo de race condition con DataIntegrityViolationException
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {

        if (userRepository.existsByDni(request.getDni())) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, null, null, "El DNI ya está registrado"));
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, null, null, "El email ya está registrado"));
        }
        if (userRepository.existsByUniversityCode(request.getUniversityCode())) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, null, null, "El código universitario ya está registrado"));
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, null, null, "El número de celular ya está registrado"));
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, null, null, "El username ya está registrado"));
        }

        // FIX: el rol siempre es USER al registrarse — el cliente no puede elegir SELLER/ADMIN
        Set<String> assignedRoles = Set.of("USER");

        // Si el correo está configurado, la cuenta nace SIN verificar y se exige confirmación.
        // Si no, se activa al instante (modo desarrollo, sin bloquear).
        boolean requiresVerification = mailService.isEnabled();
        String verificationToken = requiresVerification ? UUID.randomUUID().toString() : null;

        User user = User.builder()
                .fullName(request.getFullName())
                .dni(request.getDni())
                .email(request.getEmail())
                .universityCode(request.getUniversityCode())
                .phone(request.getPhone())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(assignedRoles)
                .enabled(!requiresVerification)
                .verificationToken(verificationToken)
                .build();

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            // FIX: maneja race condition (dos registros simultáneos con mismo DNI/email/etc.)
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AuthResponse(null, null, null, "El usuario ya existe (conflicto de datos)"));
        }

        if (requiresVerification) {
            String link = verifyBaseUrl + "/api/auth/verify?token=" + verificationToken;
            try {
                mailService.sendVerification(user.getEmail(), link);
            } catch (Exception e) {
                // No tumbamos el registro si el correo falla; el usuario podrá reintentar.
                return ResponseEntity.ok(new AuthResponse(null, null, user.getUsername(),
                        "Cuenta creada, pero no se pudo enviar el correo de verificación. Contacta al soporte."));
            }
            // Sin token: el cliente debe confirmar el correo antes de iniciar sesión.
            return ResponseEntity.ok(new AuthResponse(null, null, user.getUsername(),
                    "Te enviamos un correo para activar tu cuenta. Revisa tu bandeja."));
        }

        String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token, toRoleAuthorities(assignedRoles), user.getUsername(), null));
    }

    @GetMapping("/verify")
    @Operation(summary = "Verificar cuenta", description = "Activa la cuenta a partir del token enviado por correo y redirige al frontend")
    public ResponseEntity<Void> verify(@RequestParam String token) {
        return userRepository.findByVerificationToken(token)
                .map(user -> {
                    user.setEnabled(true);
                    user.setVerificationToken(null);
                    userRepository.save(user);
                    return ResponseEntity.status(HttpStatus.FOUND)
                            .location(URI.create(frontendUrl + "/login?verified=1"))
                            .<Void>build();
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(frontendUrl + "/login?verified=0"))
                        .build());
    }

    @Operation(summary = "Iniciar sesión", description = "Autentica usuario y retorna JWT con roles")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    @PostMapping("/login")
    // FIX: captura AuthenticationException y retorna 401 en lugar de propagar un 500
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, null, "Tu cuenta no está verificada. Revisa el correo de activación."));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, null, "Credenciales incorrectas"));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, null, "Autenticación fallida: " + e.getMessage()));
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Set<String> safeRoles = ensureUserHasAtLeastOneRole(user);
        return ResponseEntity.ok(
                new AuthResponse(jwtUtil.generateToken(user), toRoleAuthorities(safeRoles), user.getUsername(), null)
        );
    }

    @GetMapping("/validate")
    @Operation(summary = "Validar token JWT", description = "Verifica si un token es válido y devuelve la información del usuario")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(400).body(Map.of("valid", false, "error", "Token no proporcionado"));
            }

            String token = authHeader.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(token);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (!jwtUtil.validateToken(token, user)) {
                return ResponseEntity.status(401).body(Map.of("valid", false, "error", "Token inválido o expirado"));
            }

            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "username", user.getUsername(),
                    "roles", toRoleAuthorities(ensureUserHasAtLeastOneRole(user)),
                    "universityCode", user.getUniversityCode(),
                    "enabled", user.isEnabled()
            ));

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return ResponseEntity.status(401).body(Map.of("valid", false, "error", "Token expirado"));
        } catch (io.jsonwebtoken.MalformedJwtException |
                 io.jsonwebtoken.security.SignatureException e) {
            return ResponseEntity.status(400).body(Map.of("valid", false, "error", "Token malformado"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("valid", false, "error", "Error interno al validar token"));
        }
    }

    // FIX: requiere autenticación — evita enumeración de usuarios por parte de anónimos
    @GetMapping("/users/{username}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener usuario por username", description = "Devuelve datos básicos para integración entre microservicios (requiere token)")
    public ResponseEntity<Map<String, Object>> getUserByUsername(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "roles", toRoleAuthorities(ensureUserHasAtLeastOneRole(user)),
                "enabled", user.isEnabled()
        ));
    }

    // FIX: solo ADMIN puede promover a SELLER — antes estaba completamente abierto
    @PatchMapping("/users/{username}/seller")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Habilitar rol SELLER (solo ADMIN)", description = "Permite que un usuario registrado pueda vender productos")
    public ResponseEntity<Map<String, Object>> enableSellerRole(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Set<String> roles = new HashSet<>(ensureUserHasAtLeastOneRole(user));
            roles.add("SELLER");
            user.setRoles(roles);
            userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                    "roles", toRoleAuthorities(roles),
                    "message", "Rol SELLER habilitado"
            ));
    }

    // El propio usuario se convierte en vendedor (autoservicio) y recibe un token actualizado con el rol SELLER.
    @PostMapping("/become-seller")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Convertirme en vendedor", description = "Asigna el rol SELLER al usuario autenticado y devuelve un token nuevo")
    public ResponseEntity<AuthResponse> becomeSeller(org.springframework.security.core.Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Set<String> roles = new HashSet<>(ensureUserHasAtLeastOneRole(user));
        roles.add("SELLER");
        user.setRoles(roles);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token, toRoleAuthorities(roles), user.getUsername(), null));
    }

    @GetMapping("/health/external")
    @CircuitBreaker(name = "auth-service", fallbackMethod = "healthFallback")
    @Operation(summary = "Health check con Circuit Breaker", description = "Demo de resiliencia para Unidad 2")
    public ResponseEntity<Map<String, Object>> checkExternalHealth() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "external-dependency"));
    }

    public ResponseEntity<Map<String, Object>> healthFallback(Throwable t) {
        return ResponseEntity.ok(Map.of(
                "status", "DEGRADED",
                "service", "external-dependency",
                "message", "Servicio externo no disponible, pero Auth Service sigue funcionando"
        ));
    }

    @DeleteMapping("/users/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar usuario (solo ADMIN)", description = "Endpoint protegido por rol")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/test-resilience")
    @Operation(summary = "Actividad Resiliencia", description = "Prueba Circuit Breaker + Retry + Fallback")
    public ResponseEntity<Map<String, String>> testResilience(
            @RequestParam(defaultValue = "alumno@upeu.edu.pe") String email) {
        String result = resilienceService.sendWelcomeNotification(email);
        return ResponseEntity.ok(Map.of("status", "DEGRADED_OK", "message", result));
    }

    private Set<String> ensureUserHasAtLeastOneRole(User user) {
        Set<String> roles = user.getRoles() == null ? new HashSet<>() : new HashSet<>(user.getRoles());
        if (roles.isEmpty()) {
            roles.add("USER");
            user.setRoles(roles);
            userRepository.save(user);
        }
        return roles;
    }

    private List<String> toRoleAuthorities(Set<String> roles) {
        return roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .sorted()
                .toList();
    }
}
