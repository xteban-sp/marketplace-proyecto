package pe.edu.upeu.messaging_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.messaging_service.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByConversationIdOrderBySentAtAsc(UUID conversationId);
}
