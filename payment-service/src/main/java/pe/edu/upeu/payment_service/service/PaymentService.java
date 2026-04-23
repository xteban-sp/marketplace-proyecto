package pe.edu.upeu.payment_service.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import pe.edu.upeu.payment_service.dto.CreatePaymentRequest;
import pe.edu.upeu.payment_service.dto.PaymentResponse;
import pe.edu.upeu.payment_service.entity.Payment;
import pe.edu.upeu.payment_service.entity.PaymentProvider;
import pe.edu.upeu.payment_service.entity.PaymentStatus;
import pe.edu.upeu.payment_service.repository.PaymentRepository;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public PaymentResponse create(CreatePaymentRequest request) {
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
        return toResponse(paymentRepository.save(payment));
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
}
