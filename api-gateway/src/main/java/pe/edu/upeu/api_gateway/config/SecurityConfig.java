package pe.edu.upeu.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pe.edu.upeu.api_gateway.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/api/auth/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/productos/**").hasAnyAuthority("PRODUCTO_CREAR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasAnyAuthority("PRODUCTO_EDITAR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasAnyAuthority("PRODUCTO_ELIMINAR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/categorias/**").hasAnyAuthority("CATEGORIA_GESTIONAR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categorias/**").hasAnyAuthority("CATEGORIA_GESTIONAR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categorias/**").hasAnyAuthority("CATEGORIA_GESTIONAR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/auth/users/**").hasAnyAuthority("USUARIO_ELIMINAR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/auth/users/*/seller").hasAnyAuthority("USUARIO_HABILITAR_VENDEDOR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/pedidos/**").hasAnyAuthority("PEDIDO_CREAR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/pedidos/**").hasAnyAuthority("PEDIDO_ACTUALIZAR_ESTADO", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/pagos/**").hasAnyAuthority("PAGO_CREAR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/pagos/**").hasAnyAuthority("PAGO_ACTUALIZAR_ESTADO", "ROLE_ADMIN")
                        .requestMatchers(
                                "/api/pedidos/**",
                                "/api/pagos/**",
                                "/api/mensajes/**",
                                "/api/notificaciones/**",
                                "/api/recomendaciones/**",
                                "/api/resenas/**"
                        ).authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
