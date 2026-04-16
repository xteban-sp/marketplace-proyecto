package pe.edu.upeu.auth_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.auth_service.dto.LoginRequest;
import pe.edu.upeu.auth_service.dto.AuthResponse;
import pe.edu.upeu.auth_service.entity.User;
import pe.edu.upeu.auth_service.repository.UserRepository;
import pe.edu.upeu.auth_service.security.JwtUtil;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody LoginRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, null, "Username ya existe"));
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getUsername() + "@marketplace.com") // temporal
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .build();

        userRepository.save(user);
        String token = jwtUtil.generateToken(user, user.getRole());

        return ResponseEntity.ok(new AuthResponse(token, user.getRole(), user.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String token = jwtUtil.generateToken(user, user.getRole());
        return ResponseEntity.ok(new AuthResponse(token, user.getRole(), user.getUsername()));
    }
}