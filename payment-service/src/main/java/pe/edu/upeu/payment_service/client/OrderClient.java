package pe.edu.upeu.payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/api/orders/{id}")
    Map<String, Object> getOrder(@PathVariable("id") UUID id);

    @PatchMapping("/api/orders/{id}/payment-status")
    Map<String, Object> updatePaymentStatus(@PathVariable("id") UUID id,
                                            @RequestParam("paymentStatus") String paymentStatus);
}
