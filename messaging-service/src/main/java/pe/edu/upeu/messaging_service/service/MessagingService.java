package pe.edu.upeu.messaging_service.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import pe.edu.upeu.messaging_service.dto.ConversationResponse;
import pe.edu.upeu.messaging_service.dto.CreateConversationRequest;
import pe.edu.upeu.messaging_service.dto.MessageResponse;
import pe.edu.upeu.messaging_service.dto.SendMessageRequest;
import pe.edu.upeu.messaging_service.entity.Conversation;
import pe.edu.upeu.messaging_service.entity.Message;
import pe.edu.upeu.messaging_service.repository.ConversationRepository;
import pe.edu.upeu.messaging_service.repository.MessageRepository;

import java.util.List;
import java.util.UUID;

@Service
public class MessagingService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public MessagingService(ConversationRepository conversationRepository, MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    public ConversationResponse createConversation(CreateConversationRequest request) {
        Conversation conversation = conversationRepository
                .findByBuyerIdAndSellerIdAndProductId(request.getBuyerId(), request.getSellerId(), request.getProductId())
                .orElseGet(() -> {
                    Conversation newConversation = new Conversation();
                    newConversation.setBuyerId(request.getBuyerId());
                    newConversation.setSellerId(request.getSellerId());
                    newConversation.setProductId(request.getProductId());
                    return conversationRepository.save(newConversation);
                });

        return toConversationResponse(conversation);
    }

    public MessageResponse sendMessage(SendMessageRequest request) {
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new EntityNotFoundException("No se encontro la conversacion: " + request.getConversationId()));

        Message message = new Message();
        message.setConversation(conversation);
        message.setSenderId(request.getSenderId());
        message.setReceiverId(request.getReceiverId());
        message.setContent(request.getContent().trim());
        message.setReadFlag(false);
        return toMessageResponse(messageRepository.save(message));
    }

    public List<MessageResponse> listMessages(UUID conversationId) {
        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId)
                .stream()
                .map(this::toMessageResponse)
                .toList();
    }

    public List<ConversationResponse> listConversations(UUID userId) {
        return conversationRepository.findByBuyerIdOrSellerId(userId, userId)
                .stream()
                .map(this::toConversationResponse)
                .toList();
    }

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
