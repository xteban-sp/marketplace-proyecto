package pe.edu.upeu.notification_service.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.notification_service.dto.CreateNotificationRequest;
import pe.edu.upeu.notification_service.dto.NotificationResponse;
import pe.edu.upeu.notification_service.service.NotificationService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Gestión de notificaciones del sistema")
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Crear notificación para un usuario")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse create(@Valid @RequestBody CreateNotificationRequest request) {
        return notificationService.create(request);
    }

    @Operation(summary = "Listar notificaciones de un usuario")
    @GetMapping
    public List<NotificationResponse> listByUser(@RequestParam UUID usuarioId) {
        return notificationService.findByUser(usuarioId);
    }

    @Operation(summary = "Marcar notificación como leída")
    @PatchMapping("/{id}/read")
    public NotificationResponse markAsRead(@PathVariable UUID id) {
        return notificationService.markAsRead(id);
    }

    // ENDPOINT DE PRUEBA PARA ACTIVIDAD DE RESILIENCIA ===
    @GetMapping("/test-resilience")
    @CircuitBreaker(name = "notification-service", fallbackMethod = "testFallback")
    @Retry(name = "notification-service")
    @Operation(summary = "Demo Resiliencia (Actividad)", description = "Prueba Circuit Breaker + Retry + Fallback")
    public ResponseEntity<Map<String, String>> testResilience(
            @RequestParam(defaultValue = "user-123") UUID usuarioId) {

        // SIMULACIÓN DE FALLO (en producción sería notificationRepository.save())
        log.info("📡 Simulando fallo para prueba de resiliencia");
        throw new RuntimeException("Database connection timeout - simulación para actividad");
    }

    // FALLBACK para test-resilience ===
    public ResponseEntity<Map<String, String>> testFallback(UUID usuarioId, Throwable t) {
        log.warn("⚡ FALLBACK ACTIVADO para test-resilience: {}", t.getMessage());
        return ResponseEntity.ok(Map.of(
                "status", "DEGRADED",
                "message", "Notificación en cola local. Se procesará cuando la BD esté disponible.",
                "fallback", "true",
                "usuarioId", usuarioId.toString()
        ));
    }
}