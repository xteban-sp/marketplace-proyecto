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
        notification.setUserId(request.getUserId());
        notification.setType(request.getType());
        notification.setTitle(request.getTitle().trim());
        notification.setMessage(request.getMessage().trim());
        notification.setReferenceId(request.getReferenceId());
        notification.setReadFlag(false);
        return toResponse(notificationRepository.save(notification));
    }

    public List<NotificationResponse> findByUser(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public NotificationResponse markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontro la notificacion: " + id));
        notification.setReadFlag(true);
        return toResponse(notificationRepository.save(notification));
    }

    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setUserId(notification.getUserId());
        response.setType(notification.getType());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setReferenceId(notification.getReferenceId());
        response.setReadFlag(notification.isReadFlag());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
}
