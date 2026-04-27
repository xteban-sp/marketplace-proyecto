package pe.edu.upeu.messaging_service.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.messaging_service.dto.ConversationResponse;
import pe.edu.upeu.messaging_service.dto.CreateConversationRequest;
import pe.edu.upeu.messaging_service.dto.MessageResponse;
import pe.edu.upeu.messaging_service.dto.SendMessageRequest;
import pe.edu.upeu.messaging_service.service.MessagingService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/mensajes")
@RequiredArgsConstructor
@Tag(name = "Messaging", description = "Gestión de mensajes entre usuarios")
@Slf4j
public class MessagingController {

    private final MessagingService messagingService;

    @Operation(summary = "Crear conversación entre comprador y vendedor")
    @PostMapping("/conversations")
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationResponse createConversation(@Valid @RequestBody CreateConversationRequest request) {
        return messagingService.createConversation(request);
    }

    @Operation(summary = "Listar conversaciones de un usuario")
    @GetMapping("/conversations")
    public List<ConversationResponse> listConversations(@RequestParam UUID userId) {
        return messagingService.listConversations(userId);
    }

    @Operation(summary = "Enviar mensaje en una conversación")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse sendMessage(@Valid @RequestBody SendMessageRequest request) {
        return messagingService.sendMessage(request);
    }

    @Operation(summary = "Listar mensajes de una conversación")
    @GetMapping
    public List<MessageResponse> listMessages(@RequestParam UUID conversationId) {
        return messagingService.listMessages(conversationId);
    }

    // === ENDPOINT DE PRUEBA PARA ACTIVIDAD DE RESILIENCIA ===
    @GetMapping("/test-resilience")
    @CircuitBreaker(name = "messaging-service", fallbackMethod = "testFallback")
    @Retry(name = "messaging-service")
    @Operation(summary = "Demo Resiliencia (Actividad)", description = "Prueba Circuit Breaker + Retry + Fallback")
    public ResponseEntity<Map<String, String>> testResilience(
            @RequestParam(defaultValue = "conv-123") UUID conversationId) {

        // SIMULACIÓN DE FALLO (en producción sería messageRepository.save())
        log.info("📡 Simulando fallo para prueba de resiliencia");
        throw new RuntimeException("Database connection timeout - simulación para actividad");
    }

    // === FALLBACK para test-resilience ===
    public ResponseEntity<Map<String, String>> testFallback(UUID conversationId, Throwable t) {
        log.warn("FALLBACK ACTIVADO para test-resilience: {}", t.getMessage());
        return ResponseEntity.ok(Map.of(
                "status", "DEGRADED",
                "message", "Mensaje en cola local. Se procesará cuando la BD esté disponible.",
                "fallback", "true",
                "conversationId", conversationId.toString()
        ));
    }
}