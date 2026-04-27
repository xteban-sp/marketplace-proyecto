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
        String vendedorId = String.valueOf(payload.get("vendedorId"));
        String pedidoId = String.valueOf(payload.get("pedidoId"));

        notificationService.create(buildRequest(
                UUID.fromString(compradorId),
                NotificationType.ORDER,
                "Pedido creado",
                "Tu pedido fue creado correctamente y esta pendiente de pago.",
                pedidoId
        ));

        if (vendedorId != null && !vendedorId.isBlank() && !"null".equalsIgnoreCase(vendedorId)) {
            notificationService.create(buildRequest(
                    UUID.fromString(vendedorId),
                    NotificationType.ORDER,
                    "Nuevo pedido recibido",
                    "Recibiste un nuevo pedido de uno de tus productos.",
                    pedidoId
            ));
        }
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
        String vendedorId = String.valueOf(payload.get("vendedorId"));
        String pagoId = String.valueOf(payload.get("pagoId"));

        notificationService.create(buildRequest(
                UUID.fromString(compradorId),
                NotificationType.PAYMENT,
                titulo,
                mensaje,
                pagoId
        ));

        if (vendedorId != null && !vendedorId.isBlank() && !"null".equalsIgnoreCase(vendedorId)) {
            String mensajeVendedor = "Pago del pedido confirmado.";
            if ("Pago fallido".equals(titulo)) {
                mensajeVendedor = "El pago de un pedido fallo. Espera un nuevo intento del comprador.";
            }
            notificationService.create(buildRequest(
                    UUID.fromString(vendedorId),
                    NotificationType.PAYMENT,
                    titulo,
                    mensajeVendedor,
                    pagoId
            ));
        }
    }

    private CreateNotificationRequest buildRequest(UUID usuarioId,
                                                   NotificationType tipo,
                                                   String titulo,
                                                   String mensaje,
                                                   String referenciaId) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUsuarioId(usuarioId);
        request.setTipo(tipo);
        request.setTitulo(titulo);
        request.setMensaje(mensaje);
        request.setReferenciaId(referenciaId);
        return request;
    }
}
