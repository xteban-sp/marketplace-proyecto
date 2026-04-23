package pe.edu.upeu.notification_service.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import pe.edu.upeu.notification_service.dto.CreateNotificationRequest;
import pe.edu.upeu.notification_service.dto.NotificationResponse;
import pe.edu.upeu.notification_service.entity.Notification;
import pe.edu.upeu.notification_service.repository.NotificationRepository;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public NotificationResponse create(CreateNotificationRequest request) {
        Notification notification = new Notification();
        notification.setUsuarioId(request.getUsuarioId());
        notification.setTipo(request.getTipo());
        notification.setTitulo(request.getTitulo().trim());
        notification.setMensaje(request.getMensaje().trim());
        notification.setReferenciaId(request.getReferenciaId());
        notification.setLeida(false);
        return toResponse(notificationRepository.save(notification));
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
