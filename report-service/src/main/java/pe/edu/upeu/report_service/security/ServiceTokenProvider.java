package pe.edu.upeu.report_service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Genera un JWT "de servicio" (roles SERVICE + ADMIN) firmado con el secret
 * compartido, para que report-service llame a otros microservicios (auth y
 * notification) sin una peticion de usuario de por medio.
 */
@Component
public class ServiceTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${spring.application.name:report-service}")
    private String serviceName;

    public String generateServiceToken() {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        return Jwts.builder()
                .subject(serviceName)
                .claim("roles", List.of("SERVICE", "ADMIN"))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 300_000))
                .signWith(key)
                .compact();
    }
}
