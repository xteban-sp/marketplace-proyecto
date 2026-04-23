package pe.edu.upeu.messaging_service.service;

import org.springframework.stereotype.Service;
import pe.edu.upeu.messaging_service.dto.MessageRequest;
import pe.edu.upeu.messaging_service.dto.MessageResponse;
import pe.edu.upeu.messaging_service.model.MessageEntity;
import pe.edu.upeu.messaging_service.repository.MessageRepository;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public MessageResponse send(MessageRequest request) {
        MessageEntity entity = new MessageEntity();
        entity.setSenderUsername(request.getSenderUsername());
        entity.setReceiverUsername(request.getReceiverUsername());
        entity.setContent(request.getContent().trim());
        return toResponse(messageRepository.save(entity));
    }

    public List<MessageResponse> getConversation(String userA, String userB) {
        return messageRepository
                .findBySenderUsernameAndReceiverUsernameOrReceiverUsernameAndSenderUsername(userA, userB, userA, userB)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private MessageResponse toResponse(MessageEntity entity) {
        MessageResponse response = new MessageResponse();
        response.setId(entity.getId());
        response.setSenderUsername(entity.getSenderUsername());
        response.setReceiverUsername(entity.getReceiverUsername());
        response.setContent(entity.getContent());
        response.setSentAt(entity.getSentAt());
        return response;
    }
}
