package pe.edu.upeu.notification_service.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.notification_service.dto.CreateNotificationRequest;
import pe.edu.upeu.notification_service.dto.NotificationResponse;
import pe.edu.upeu.notification_service.entity.Notification;
import pe.edu.upeu.notification_service.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @CircuitBreaker(name = "notification-service", fallbackMethod = "createFallback")
    @Retry(name = "notification-service", fallbackMethod = "createFallback")
    public NotificationResponse create(CreateNotificationRequest request) {
        log.info("📡 Intentando guardar notificación para usuario: {}", request.getUsuarioId());

        Notification notification = new Notification();
        notification.setUsuarioId(request.getUsuarioId());
        notification.setTipo(request.getTipo());
        notification.setTitulo(request.getTitulo().trim());
        notification.setMensaje(request.getMensaje().trim());
        notification.setReferenciaId(request.getReferenciaId());
        notification.setLeida(false);

        return toResponse(notificationRepository.save(notification));
    }

    public NotificationResponse createFallback(CreateNotificationRequest request, Throwable t) {
        log.warn("🛡️ FALLBACK ACTIVADO para usuario {}: {}",
                request.getUsuarioId(), t.getMessage());

        // Respuesta degradada pero funcional
        NotificationResponse degraded = new NotificationResponse();
        degraded.setId(UUID.randomUUID());
        degraded.setUsuarioId(request.getUsuarioId());
        degraded.setTipo(request.getTipo());
        degraded.setTitulo(request.getTitulo());
        degraded.setMensaje("Notificación en cola pendiente. Se procesará cuando el sistema esté estable.");
        degraded.setReferenciaId(request.getReferenciaId());
        degraded.setLeida(false);
        degraded.setCreatedAt(LocalDateTime.now());

        return degraded;
    }

    public List<NotificationResponse> findByUser(UUID usuarioId) {
        return notificationRepository.findByUsuarioIdOrderByCreatedAtDesc(usuarioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public NotificationResponse markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontro la notificacion: " + id));
        notification.setLeida(true);
        return toResponse(notificationRepository.save(notification));
    }

    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setUsuarioId(notification.getUsuarioId());
        response.setTipo(notification.getTipo());
        response.setTitulo(notification.getTitulo());
        response.setMensaje(notification.getMensaje());
        response.setReferenciaId(notification.getReferenciaId());
        response.setLeida(notification.isLeida());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
}