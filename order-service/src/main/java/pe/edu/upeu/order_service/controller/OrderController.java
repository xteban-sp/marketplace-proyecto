package pe.edu.upeu.order_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.order_service.dto.CreateOrderRequest;
import pe.edu.upeu.order_service.dto.OrderResponse;
import pe.edu.upeu.order_service.entity.OrderStatus;
import pe.edu.upeu.order_service.entity.PaymentStatus;
import pe.edu.upeu.order_service.service.OrderService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/pedidos")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.create(request);
    }

    @GetMapping("/{id}")
    public OrderResponse findById(@PathVariable UUID id) {
        return orderService.findById(id);
    }

    @GetMapping
    public List<OrderResponse> findAll(@RequestParam(required = false) UUID compradorId,
                                       @RequestParam(required = false) UUID vendedorId) {
        return orderService.findAll(compradorId, vendedorId);
    }

    @PatchMapping("/{id}/estado")
    public OrderResponse updateStatus(@PathVariable UUID id, @RequestParam OrderStatus estado) {
        return orderService.updateStatus(id, estado);
    }

    @PatchMapping("/{id}/estado-pago")
    public OrderResponse updatePaymentStatus(@PathVariable UUID id, @RequestParam PaymentStatus estadoPago) {
        return orderService.updatePaymentStatus(id, estadoPago);
    }

    @GetMapping("/{id}/users/{userId}/review-eligible")
    public Map<String, Boolean> isReviewEligible(@PathVariable UUID id, @PathVariable UUID userId) {
        return Map.of("eligible", orderService.isReviewEnabled(id, userId));
    }

    @GetMapping("/{id}/usuarios/{usuarioId}/resena-habilitada")
    public Map<String, Boolean> isResenaHabilitada(@PathVariable UUID id, @PathVariable UUID usuarioId) {
        return Map.of("habilitada", orderService.isReviewEnabled(id, usuarioId));
    }
}
