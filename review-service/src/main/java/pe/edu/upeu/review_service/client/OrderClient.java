package pe.edu.upeu.review_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/api/orders/{id}/users/{userId}/review-eligible")
    Map<String, Boolean> isReviewEligible(@PathVariable("id") UUID orderId,
                                          @PathVariable("userId") UUID userId);
}
