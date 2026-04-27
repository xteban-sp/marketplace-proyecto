package pe.edu.upeu.auth_service.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ResilienceService {

    @CircuitBreaker(name = "auth-service", fallbackMethod = "notifyFallback")
    @Retry(name = "auth-service")
    public String sendWelcomeNotification(String email) {
        log.info("Intentando enviar notificación a: {}", email);

        // SIMULACIÓN DE FALLO (en producción sería Feign/RestTemplate)
        throw new RuntimeException("Timeout: Notification Service no responde");
    }

    // FALLBACK: se ejecuta tras agotar reintentos o abrir circuito
    public String notifyFallback(String email, Throwable t) {
        log.warn("FALLBACK ACTIVADO para {}: {}", email, t.getMessage());
        return "Notificación almacenada en cola local. Se reintentará en segundo plano.";
    }
}