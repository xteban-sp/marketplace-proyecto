package pe.edu.upeu.messaging_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.messaging_service.entity.Conversation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    Optional<Conversation> findByBuyerIdAndSellerIdAndProductId(UUID buyerId, UUID sellerId, Long productId);
    List<Conversation> findByBuyerIdOrSellerId(UUID buyerId, UUID sellerId);
}
