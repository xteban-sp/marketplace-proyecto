package pe.edu.upeu.auth_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
import java.util.*;

@RestController
@RequestMapping("/auth")
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

        // === Validaciones de unicidad ===
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

        // === Asignar los roles ===
        Set<String> validRoles = Set.of("USER", "SELLER", "ADMIN");
        Set<String> assignedRoles = (request.getRoles() != null && !request.getRoles().isEmpty() && validRoles.containsAll(request.getRoles()))
                ? request.getRoles()
                : Set.of("USER");

        // === Crear un usuario ===
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
            // 1. Extraer el token del header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(400).body(Map.of("valid", false, "error", "Token no proporcionado"));
            }

            String token = authHeader.replace("Bearer ", "");

            // 2. Extraer username del token usando tu JwtUtil
            String username = jwtUtil.extractUsername(token);

            // 3. Cargar usuario desde BD para validar que existe y está activo
            pe.edu.upeu.auth_service.entity.User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // 4. Validar que el token no haya expirado y sea válido
            if (!jwtUtil.validateToken(token, user)) {
                return ResponseEntity.status(401).body(Map.of("valid", false, "error", "Token inválido o expirado"));
            }

            // 5. Todo bien: devolver información real del usuario
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
}