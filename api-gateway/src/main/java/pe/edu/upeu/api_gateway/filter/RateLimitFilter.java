package pe.edu.upeu.api_gateway.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

/**
 * Rate limiting distribuido en el API Gateway.
 *
 * Algoritmo: ventana fija por cliente (IP) usando un contador en Redis
 * (INCR + EXPIRE). Al estar en Redis, el límite es compartido entre todas las
 * réplicas del gateway. Si Redis falla, se aplica "fail-open" (no se bloquea).
 *
 * Configurable por variables de entorno:
 *   RATELIMIT_ENABLED, RATELIMIT_CAPACITY, RATELIMIT_WINDOW_SECONDS
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redis;

    @Value("${ratelimit.enabled:true}")
    private boolean enabled;

    @Value("${ratelimit.capacity:100}")
    private long capacity;

    @Value("${ratelimit.window-seconds:60}")
    private long windowSeconds;

    public RateLimitFilter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        // No limitamos health/metrics ni el preflight CORS.
        if (!enabled || "OPTIONS".equalsIgnoreCase(request.getMethod()) || path.startsWith("/actuator")) {
            chain.doFilter(request, response);
            return;
        }

        String client = clientKey(request);
        long window = Instant.now().getEpochSecond() / windowSeconds;
        String key = "rl:" + client + ":" + window;

        long current;
        try {
            Long count = redis.opsForValue().increment(key);
            current = (count == null) ? 0 : count;
            if (current == 1L) {
                redis.expire(key, Duration.ofSeconds(windowSeconds));
            }
        } catch (Exception ex) {
            // Fail-open: si Redis no responde, dejamos pasar para no tumbar el tráfico.
            chain.doFilter(request, response);
            return;
        }

        long remaining = Math.max(0, capacity - current);
        response.setHeader("X-RateLimit-Limit", String.valueOf(capacity));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));

        if (current > capacity) {
            response.setStatus(429); // 429 Too Many Requests
            response.setHeader("Retry-After", String.valueOf(windowSeconds));
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":429,\"error\":\"TOO_MANY_REQUESTS\",\"message\":\"Superaste el límite de "
                    + capacity + " peticiones cada " + windowSeconds + " s. Intenta más tarde.\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    /** Identifica al cliente: primero X-Forwarded-For (detrás de Nginx/Caddy), si no, la IP remota. */
    private String clientKey(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
