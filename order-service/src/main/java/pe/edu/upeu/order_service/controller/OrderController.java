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
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
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
    public List<OrderResponse> findAll(@RequestParam(required = false) UUID buyerId,
                                       @RequestParam(required = false) UUID sellerId) {
        return orderService.findAll(buyerId, sellerId);
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable UUID id, @RequestParam OrderStatus status) {
        return orderService.updateStatus(id, status);
    }

    @PatchMapping("/{id}/payment-status")
    public OrderResponse updatePaymentStatus(@PathVariable UUID id, @RequestParam PaymentStatus paymentStatus) {
        return orderService.updatePaymentStatus(id, paymentStatus);
    }
}
