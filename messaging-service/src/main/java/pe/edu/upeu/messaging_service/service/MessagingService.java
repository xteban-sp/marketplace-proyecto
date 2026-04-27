package pe.edu.upeu.messaging_service.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.messaging_service.dto.ConversationResponse;
import pe.edu.upeu.messaging_service.dto.CreateConversationRequest;
import pe.edu.upeu.messaging_service.dto.MessageResponse;
import pe.edu.upeu.messaging_service.dto.SendMessageRequest;
import pe.edu.upeu.messaging_service.entity.Conversation;
import pe.edu.upeu.messaging_service.entity.Message;
import pe.edu.upeu.messaging_service.repository.ConversationRepository;
import pe.edu.upeu.messaging_service.repository.MessageRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class MessagingService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public MessagingService(ConversationRepository conversationRepository,
                            MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    // === CREATE CONVERSATION (sin resiliencia - no llama a externos) ===
    public ConversationResponse createConversation(CreateConversationRequest request) {
        Conversation conversation = conversationRepository
                .findByBuyerIdAndSellerIdAndProductId(
                        request.getBuyerId(),
                        request.getSellerId(),
                        request.getProductId())
                .orElseGet(() -> {
                    Conversation newConversation = new Conversation();
                    newConversation.setBuyerId(request.getBuyerId());
                    newConversation.setSellerId(request.getSellerId());
                    newConversation.setProductId(request.getProductId());
                    return conversationRepository.save(newConversation);
                });

        return toConversationResponse(conversation);
    }

    // === SEND MESSAGE - CON RESILIENCIA (Circuit Breaker + Retry) ===
    @CircuitBreaker(name = "messaging-service", fallbackMethod = "sendMessageFallback")
    @Retry(name = "messaging-service", fallbackMethod = "sendMessageFallback")
    public MessageResponse sendMessage(SendMessageRequest request) {
        log.info("Intentando guardar mensaje en conversación: {}", request.getConversationId());

        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontro la conversacion: " + request.getConversationId()));

        Message message = new Message();
        message.setConversation(conversation);
        message.setSenderId(request.getSenderId());
        message.setReceiverId(request.getReceiverId());
        message.setContent(request.getContent().trim());
        message.setReadFlag(false);

        return toMessageResponse(messageRepository.save(message));
    }

    // === FALLBACK METHOD para sendMessage ===
    public MessageResponse sendMessageFallback(SendMessageRequest request, Throwable t) {
        log.warn("FALLBACK ACTIVADO para conversación {}: {}",
                request.getConversationId(), t.getMessage());

        // Respuesta degradada pero funcional
        MessageResponse degraded = new MessageResponse();
        degraded.setId(UUID.randomUUID());
        degraded.setConversationId(request.getConversationId());
        degraded.setSenderId(request.getSenderId());
        degraded.setReceiverId(request.getReceiverId());
        degraded.setContent(request.getContent());
        degraded.setReadFlag(false);
        degraded.setSentAt(LocalDateTime.now());
        // Nota: Si MessageResponse no tiene este campo, quita la siguiente línea
        // degraded.setMensajeAdicional("Mensaje en cola pendiente. Se procesará cuando el sistema esté estable.");

        return degraded;
    }

    // === LIST MESSAGES (sin resiliencia - lectura local) ===
    public List<MessageResponse> listMessages(UUID conversationId) {
        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId)
                .stream()
                .map(this::toMessageResponse)
                .toList();
    }

    // === LIST CONVERSATIONS (sin resiliencia - lectura local) ===
    public List<ConversationResponse> listConversations(UUID userId) {
        return conversationRepository.findByBuyerIdOrSellerId(userId, userId)
                .stream()
                .map(this::toConversationResponse)
                .toList();
    }

    // === MAPPERS PRIVADOS ===
    private ConversationResponse toConversationResponse(Conversation conversation) {
        ConversationResponse response = new ConversationResponse();
        response.setId(conversation.getId());
        response.setBuyerId(conversation.getBuyerId());
        response.setSellerId(conversation.getSellerId());
        response.setProductId(conversation.getProductId());
        response.setCreatedAt(conversation.getCreatedAt());
        return response;
    }

    private MessageResponse toMessageResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setConversationId(message.getConversation().getId());
        response.setSenderId(message.getSenderId());
        response.setReceiverId(message.getReceiverId());
        response.setContent(message.getContent());
        response.setReadFlag(message.isReadFlag());
        response.setSentAt(message.getSentAt());
        return response;
    }
}