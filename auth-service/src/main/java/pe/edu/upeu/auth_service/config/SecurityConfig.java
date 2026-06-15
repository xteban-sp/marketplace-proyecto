package pe.edu.upeu.auth_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pe.edu.upeu.auth_service.filter.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // CORS se maneja de forma centralizada en el api-gateway (unico punto de entrada del SPA).
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // FIX: rutas granulares — antes todo /api/auth/** era público,
                        // lo que expona endpoints de administración sin autenticación.
                        // Ahora solo login, register y validate son públicos.
                        .requestMatchers(HttpMethod.POST,
                                "/auth/register", "/api/auth/register",
                                "/auth/login",    "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/auth/validate", "/api/auth/validate",
                                "/auth/verify", "/api/auth/verify",
                                "/auth/health/external", "/api/auth/health/external").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        // Todo lo demás (incluye /users/**, /users/{u}/seller, etc.) requiere JWT
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
