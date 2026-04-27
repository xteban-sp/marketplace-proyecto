package pe.edu.upeu.auth_service.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.auth_service.dto.LoginRequest;
import pe.edu.upeu.auth_service.dto.RegisterRequest;
import pe.edu.upeu.auth_service.dto.AuthResponse;
import pe.edu.upeu.auth_service.entity.User;
import pe.edu.upeu.auth_service.repository.UserRepository;
import pe.edu.upeu.auth_service.security.JwtUtil;
import pe.edu.upeu.auth_service.service.ResilienceService;

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

    @Operation(summary = "Registrar nuevo usuario", description = "Crea cuenta con validación de DNI, email, código universitario y celular")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro exitoso"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o usuario ya existe")
    })
    @PostMapping("/register")
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

        Set<String> validRoles = Set.of("USER", "SELLER", "ADMIN");
        Set<String> assignedRoles = (request.getRoles() != null && !request.getRoles().isEmpty() && validRoles.containsAll(request.getRoles()))
                ? request.getRoles()
                : Set.of("USER");

        User user = User.builder()
                .fullName(request.getFullName())
                .dni(request.getDni())
                .email(request.getEmail())
                .universityCode(request.getUniversityCode())
                .phone(request.getPhone())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(assignedRoles)
                .enabled(true)
                .build();

        userRepository.save(user);
        String token = jwtUtil.generateToken(user);

        return ResponseEntity.ok(new AuthResponse(token, new ArrayList<>(assignedRoles), user.getUsername(), null));
    }

    @Operation(summary = "Iniciar sesión", description = "Autentica usuario y retorna JWT con roles")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(
                new AuthResponse(jwtUtil.generateToken(user), new ArrayList<>(user.getRoles()), user.getUsername(), null)
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

            pe.edu.upeu.auth_service.entity.User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (!jwtUtil.validateToken(token, user)) {
                return ResponseEntity.status(401).body(Map.of("valid", false, "error", "Token inválido o expirado"));
            }

            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "username", user.getUsername(),
                    "roles", new ArrayList<>(user.getRoles()),
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

    @GetMapping("/users/{username}")
    @Operation(summary = "Obtener usuario por username", description = "Devuelve datos basicos para integracion entre microservicios")
    public ResponseEntity<Map<String, Object>> getUserByUsername(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "roles", new ArrayList<>(user.getRoles()),
                "enabled", user.isEnabled()
        ));
    }

    @PatchMapping("/users/{username}/seller")
    @Operation(summary = "Habilitar rol SELLER", description = "Permite que un usuario registrado pueda vender productos")
    public ResponseEntity<Map<String, Object>> enableSellerRole(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Set<String> roles = new HashSet<>(user.getRoles());
        roles.add("SELLER");
        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "roles", new ArrayList<>(user.getRoles()),
                "message", "Rol SELLER habilitado"
        ));
    }

    @GetMapping("/health/external")
    @CircuitBreaker(name = "auth-service", fallbackMethod = "healthFallback")
    @Operation(summary = "Health check con Circuit Breaker", description = "Demo de resiliencia para Unidad 2")
    public ResponseEntity<Map<String, Object>> checkExternalHealth() {
        // Simula llamada a otro servicio (ej: Notification Service)
        // En producción: notificationClient.ping()
        return ResponseEntity.ok(Map.of("status", "UP", "service", "external-dependency"));
    }

    // Fallback method (se ejecuta si el circuito está abierto)
    public ResponseEntity<Map<String, Object>> healthFallback(Throwable t) {
        return ResponseEntity.ok(Map.of(
                "status", "DEGRADED",
                "service", "external-dependency",
                "message", "Servicio externo no disponible, pero Auth Service sigue funcionando"
        ));
    }

    // Solo ADMIN puede eliminar usuarios
    @DeleteMapping("/users/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar usuario (solo ADMIN)", description = "Endpoint protegido por rol")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }

    // 1. Inyecta el service en el constructor (si usas @RequiredArgsConstructor, ya está)
    private final ResilienceService resilienceService;

    // 2. Agrega este endpoint:
    @GetMapping("/test-resilience")
    @Operation(summary = "Actividad Resiliencia", description = "Prueba Circuit Breaker + Retry + Fallback")
    public ResponseEntity<Map<String, String>> testResilience(
            @RequestParam(defaultValue = "alumno@upeu.edu.pe") String email) {

        String result = resilienceService.sendWelcomeNotification(email);
        return ResponseEntity.ok(Map.of("status", "DEGRADED_OK", "message", result));
    }


}
