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
import pe.edu.upeu.messaging_service.dto.MessageRequest;
import pe.edu.upeu.messaging_service.dto.MessageResponse;
import pe.edu.upeu.messaging_service.service.MessageService;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse send(@Valid @RequestBody MessageRequest request) {
        return messageService.send(request);
    }

    @GetMapping("/conversation")
    public List<MessageResponse> conversation(@RequestParam String userA, @RequestParam String userB) {
        return messageService.getConversation(userA, userB);
    }
}
