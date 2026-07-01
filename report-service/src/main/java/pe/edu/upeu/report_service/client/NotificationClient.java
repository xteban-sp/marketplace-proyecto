package pe.edu.upeu.report_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@FeignClient(name = "notification-service")
public interface NotificationClient {
    // Crea una notificacion (campanita). Body: {usuarioId, tipo, titulo, mensaje, referenciaId}
    @PostMapping("/api/notificaciones")
    Map<String, Object> crear(@RequestBody Map<String, Object> body);
}
