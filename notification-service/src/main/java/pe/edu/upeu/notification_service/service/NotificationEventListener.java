package pe.edu.upeu.notification_service.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.edu.upeu.notification_service.dto.CreateNotificationRequest;
import pe.edu.upeu.notification_service.entity.NotificationType;

import java.util.Map;
import java.util.UUID;

@Component
public class NotificationEventListener {

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "pedido-creado", groupId = "notification-service")
    public void onPedidoCreado(Map<String, Object> payload) {
        String compradorId = String.valueOf(payload.get("compradorId"));
        String pedidoId = String.valueOf(payload.get("pedidoId"));

        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(UUID.fromString(compradorId));
        request.setType(NotificationType.ORDER);
        request.setTitle("Pedido creado");
        request.setMessage("Tu pedido fue creado correctamente y esta pendiente de pago.");
        request.setReferenceId(pedidoId);
        notificationService.create(request);
    }

    @KafkaListener(topics = "pago-aprobado", groupId = "notification-service")
    public void onPagoAprobado(Map<String, Object> payload) {
        crearNotificacionPago(payload, "Pago aprobado", "Tu pago fue aprobado con exito.");
    }

    @KafkaListener(topics = "pago-fallido", groupId = "notification-service")
    public void onPagoFallido(Map<String, Object> payload) {
        crearNotificacionPago(payload, "Pago fallido", "Tu pago fue rechazado o fallo. Intenta nuevamente.");
    }

    private void crearNotificacionPago(Map<String, Object> payload, String titulo, String mensaje) {
        String compradorId = String.valueOf(payload.get("compradorId"));
        String pagoId = String.valueOf(payload.get("pagoId"));

        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(UUID.fromString(compradorId));
        request.setType(NotificationType.PAYMENT);
        request.setTitle(titulo);
        request.setMessage(mensaje);
        request.setReferenceId(pagoId);
        notificationService.create(request);
    }
}
