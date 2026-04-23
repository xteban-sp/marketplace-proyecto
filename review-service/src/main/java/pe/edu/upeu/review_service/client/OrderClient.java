package pe.edu.upeu.review_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/api/orders/{id}/user/{username}/review-eligible")
    Map<String, Boolean> isReviewEligible(@PathVariable("id") Long orderId, @PathVariable("username") String username);
}
