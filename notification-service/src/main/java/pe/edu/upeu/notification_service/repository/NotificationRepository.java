package pe.edu.upeu.notification_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.notification_service.entity.Notification;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
