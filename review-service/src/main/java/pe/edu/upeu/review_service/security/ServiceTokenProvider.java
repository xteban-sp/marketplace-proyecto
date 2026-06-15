package pe.edu.upeu.review_service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Genera un JWT "de servicio" firmado con el secret compartido, para las
 * llamadas entre microservicios que no se originan en una peticion de usuario.
 * Lleva roles SERVICE y ADMIN.
 */
@Component
public class ServiceTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${spring.application.name:review-service}")
    private String serviceName;

    public String generateServiceToken() {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        return Jwts.builder()
                .subject(serviceName)
                .claim("roles", List.of("SERVICE", "ADMIN"))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 300_000)) // 5 min
                .signWith(key)
                .compact();
    }
}
