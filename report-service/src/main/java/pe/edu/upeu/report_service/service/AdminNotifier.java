package pe.edu.upeu.report_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.edu.upeu.report_service.client.AuthClient;
import pe.edu.upeu.report_service.client.NotificationClient;

import java.util.Map;

/**
 * Envia notificaciones de reportes a la CAMPANITA del admin. Resuelve el id del
 * admin una vez (via auth-service) y lo cachea. Es "best-effort": si algo falla,
 * NO interrumpe el procesamiento de eventos.
 */
@Slf4j
@Service
public class AdminNotifier {

    private final AuthClient authClient;
    private final NotificationClient notificationClient;

    @Value("${report.admin-username:admin}")
    private String adminUsername;

    @Value("${report.notify-enabled:true}")
    private boolean notifyEnabled;

    private volatile String adminIdCache;

    public AdminNotifier(AuthClient authClient, NotificationClient notificationClient) {
        this.authClient = authClient;
        this.notificationClient = notificationClient;
    }

    public void notificarVenta(String monto, String pedidoId) {
        if (!notifyEnabled) return;
        try {
            String adminId = resolveAdminId();
            if (adminId == null) return;
            notificationClient.crear(Map.of(
                    "usuarioId", adminId,
                    "tipo", "PAYMENT",
                    "titulo", "Nueva venta",
                    "mensaje", "Se registró una venta por S/ " + monto + ".",
                    "referenciaId", pedidoId == null ? "reporte" : pedidoId
            ));
        } catch (Exception ex) {
            log.warn("No se pudo notificar la venta al admin: {}", ex.getMessage());
        }
    }

    private String resolveAdminId() {
        if (adminIdCache != null) return adminIdCache;
        try {
            Map<String, Object> user = authClient.getUserByUsername(adminUsername);
            if (user != null && user.get("id") != null) {
                adminIdCache = String.valueOf(user.get("id"));
            }
        } catch (Exception ex) {
            log.warn("No se pudo resolver el id del admin '{}': {}", adminUsername, ex.getMessage());
        }
        return adminIdCache;
    }
}
