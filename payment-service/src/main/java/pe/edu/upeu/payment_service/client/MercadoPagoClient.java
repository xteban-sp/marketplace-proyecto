package pe.edu.upeu.payment_service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cliente real de Mercado Pago (Checkout Pro). Crea una "preferencia" de pago y
 * devuelve el id y el init_point (URL de checkout real).
 *
 * Si no hay access-token configurado, queda DESHABILITADO y el PaymentService usa
 * un modo simulado, para que el proyecto siga funcionando sin credenciales.
 *
 * Para activarlo (gratis): crear app en https://www.mercadopago.com.pe/developers,
 * obtener el Access Token de PRUEBA y exponerlo como MERCADOPAGO_ACCESS_TOKEN.
 */
@Component
public class MercadoPagoClient {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoClient.class);

    private final RestTemplate restTemplate;

    @Value("${mercadopago.access-token:}")
    private String accessToken;

    @Value("${mercadopago.base-url:https://api.mercadopago.com}")
    private String baseUrl;

    @Value("${mercadopago.currency-id:PEN}")
    private String currencyId;

    @Value("${mercadopago.success-url:http://localhost:3000/pago/exito}")
    private String successUrl;

    @Value("${mercadopago.pending-url:http://localhost:3000/pago/pendiente}")
    private String pendingUrl;

    @Value("${mercadopago.failure-url:http://localhost:3000/pago/error}")
    private String failureUrl;

    // Debe ser una URL PUBLICA para que Mercado Pago pueda notificar. En local se deja vacia.
    @Value("${mercadopago.notification-url:}")
    private String notificationUrl;

    public MercadoPagoClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean isEnabled() {
        return accessToken != null && !accessToken.isBlank();
    }

    @SuppressWarnings("unchecked")
    public MpPreference createPreference(String title, BigDecimal amount, String externalReference) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("title", title);
        item.put("quantity", 1);
        item.put("currency_id", currencyId);
        item.put("unit_price", amount);

        List<Map<String, Object>> items = new ArrayList<>();
        items.add(item);

        Map<String, Object> backUrls = new LinkedHashMap<>();
        backUrls.put("success", successUrl);
        backUrls.put("pending", pendingUrl);
        backUrls.put("failure", failureUrl);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", items);
        body.put("external_reference", externalReference);
        body.put("back_urls", backUrls);
        body.put("auto_return", "approved");
        if (notificationUrl != null && !notificationUrl.isBlank()) {
            body.put("notification_url", notificationUrl);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/checkout/preferences",
                new HttpEntity<>(body, headers),
                Map.class
        );

        Map<String, Object> respBody = response.getBody();
        if (respBody == null || respBody.get("id") == null) {
            throw new IllegalStateException("Mercado Pago no devolvio una preferencia valida");
        }
        String id = String.valueOf(respBody.get("id"));
        Object initPoint = respBody.getOrDefault("init_point", respBody.get("sandbox_init_point"));
        log.info("Preferencia Mercado Pago creada: {}", id);
        return new MpPreference(id, initPoint != null ? String.valueOf(initPoint) : null);
    }

    public record MpPreference(String id, String initPoint) {
    }
}
