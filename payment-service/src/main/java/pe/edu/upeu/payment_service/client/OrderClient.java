package pe.edu.upeu.payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;

@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/api/orders/{id}")
    Map<String, Object> getOrder(@PathVariable("id") Long id);

    @PatchMapping("/api/orders/{id}/payment-status")
    Map<String, Object> updatePaymentStatus(@PathVariable("id") Long id, @RequestParam("status") String status);
}
