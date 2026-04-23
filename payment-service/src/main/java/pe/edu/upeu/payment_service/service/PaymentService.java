package pe.edu.upeu.payment_service.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pe.edu.upeu.payment_service.client.OrderClient;
import pe.edu.upeu.payment_service.dto.CreatePaymentRequest;
import pe.edu.upeu.payment_service.dto.PaymentResponse;
import pe.edu.upeu.payment_service.entity.Payment;
import pe.edu.upeu.payment_service.entity.PaymentProvider;
import pe.edu.upeu.payment_service.entity.PaymentStatus;
import pe.edu.upeu.payment_service.repository.PaymentRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentService(PaymentRepository paymentRepository,
                          OrderClient orderClient,
                          KafkaTemplate<String, Object> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.orderClient = orderClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public PaymentResponse create(CreatePaymentRequest request) {
        Map<String, Object> pedido = orderClient.getOrder(request.getOrderId());
        if (pedido == null || pedido.isEmpty()) {
            throw new EntityNotFoundException("No se encontro el pedido asociado al pago");
        }

        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setBuyerId(request.getBuyerId());
        payment.setAmount(request.getAmount());
        payment.setProvider(PaymentProvider.MERCADO_PAGO);
        payment.setStatus(PaymentStatus.PENDING);

        String reference = "mp-order-" + request.getOrderId();
        payment.setExternalReference(reference);
        payment.setPreferenceId(UUID.randomUUID().toString());
        payment.setCheckoutUrl("https://www.mercadopago.com/checkout/v1/redirect?pref_id=" + payment.getPreferenceId());

        return toResponse(paymentRepository.save(payment));
    }

    public PaymentResponse findById(UUID id) {
        return toResponse(getEntity(id));
    }

    public List<PaymentResponse> findByOrder(UUID orderId) {
        return paymentRepository.findByOrderId(orderId).stream().map(this::toResponse).toList();
    }

    public PaymentResponse updateStatus(UUID id, PaymentStatus status) {
        Payment payment = getEntity(id);
        payment.setStatus(status);
        Payment guardado = paymentRepository.save(payment);

        if (status == PaymentStatus.APPROVED) {
            orderClient.updatePaymentStatus(guardado.getOrderId(), "APPROVED");
            kafkaTemplate.send("pago-aprobado", guardado.getId().toString(), construirEvento(guardado));
        }
        if (status == PaymentStatus.FAILED) {
            orderClient.updatePaymentStatus(guardado.getOrderId(), "FAILED");
            kafkaTemplate.send("pago-fallido", guardado.getId().toString(), construirEvento(guardado));
        }

        return toResponse(guardado);
    }

    public PaymentResponse procesarWebhookMercadoPago(String externalReference, String status) {
        Payment payment = paymentRepository.findByExternalReference(externalReference)
                .orElseThrow(() -> new EntityNotFoundException("No se encontro pago para la referencia externa: " + externalReference));

        PaymentStatus newStatus;
        if ("approved".equalsIgnoreCase(status)) {
            newStatus = PaymentStatus.APPROVED;
        } else if ("rejected".equalsIgnoreCase(status) || "failed".equalsIgnoreCase(status)) {
            newStatus = PaymentStatus.FAILED;
        } else {
            newStatus = PaymentStatus.PENDING;
        }

        return updateStatus(payment.getId(), newStatus);
    }

    private Payment getEntity(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontro el pago con id: " + id));
    }

    private PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrderId());
        response.setBuyerId(payment.getBuyerId());
        response.setAmount(payment.getAmount());
        response.setProvider(payment.getProvider());
        response.setStatus(payment.getStatus());
        response.setExternalReference(payment.getExternalReference());
        response.setPreferenceId(payment.getPreferenceId());
        response.setCheckoutUrl(payment.getCheckoutUrl());
        response.setCreatedAt(payment.getCreatedAt());
        return response;
    }

    private Map<String, Object> construirEvento(Payment payment) {
        return Map.of(
                "pagoId", payment.getId().toString(),
                "pedidoId", payment.getOrderId().toString(),
                "compradorId", payment.getBuyerId().toString(),
                "estado", payment.getStatus().name(),
                "monto", payment.getAmount()
        );
    }
}
