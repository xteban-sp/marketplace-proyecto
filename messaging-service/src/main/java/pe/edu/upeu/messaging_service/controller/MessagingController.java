package pe.edu.upeu.messaging_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.messaging_service.dto.ConversationResponse;
import pe.edu.upeu.messaging_service.dto.CreateConversationRequest;
import pe.edu.upeu.messaging_service.dto.MessageResponse;
import pe.edu.upeu.messaging_service.dto.SendMessageRequest;
import pe.edu.upeu.messaging_service.service.MessagingService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
public class MessagingController {

    private final MessagingService messagingService;

    public MessagingController(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @PostMapping("/conversations")
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationResponse createConversation(@Valid @RequestBody CreateConversationRequest request) {
        return messagingService.createConversation(request);
    }

    @GetMapping("/conversations")
    public List<ConversationResponse> listConversations(@RequestParam UUID userId) {
        return messagingService.listConversations(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse sendMessage(@Valid @RequestBody SendMessageRequest request) {
        return messagingService.sendMessage(request);
    }

    @GetMapping
    public List<MessageResponse> listMessages(@RequestParam UUID conversationId) {
        return messagingService.listMessages(conversationId);
    }
}
