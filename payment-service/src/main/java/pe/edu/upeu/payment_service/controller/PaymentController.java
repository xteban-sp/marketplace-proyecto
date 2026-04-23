package pe.edu.upeu.payment_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.payment_service.dto.CreatePaymentRequest;
import pe.edu.upeu.payment_service.dto.PaymentResponse;
import pe.edu.upeu.payment_service.entity.PaymentStatus;
import pe.edu.upeu.payment_service.service.PaymentService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pagos")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse create(@Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.create(request);
    }

    @GetMapping("/{id}")
    public PaymentResponse findById(@PathVariable UUID id) {
        return paymentService.findById(id);
    }

    @GetMapping
    public List<PaymentResponse> findByOrder(@RequestParam UUID pedidoId) {
        return paymentService.findByOrder(pedidoId);
    }

    @PatchMapping("/{id}/status")
    public PaymentResponse updateStatus(@PathVariable UUID id, @RequestParam PaymentStatus status) {
        return paymentService.updateStatus(id, status);
    }

    @PostMapping("/webhook/mercadopago")
    public PaymentResponse webhookMercadoPago(@RequestParam String externalReference,
                                              @RequestParam String status,
                                              @RequestHeader(value = "x-signature", required = false) String signature) {
        return paymentService.procesarWebhookMercadoPago(externalReference, status);
    }
}
