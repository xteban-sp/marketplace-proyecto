package pe.edu.upeu.payment_service.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    @CircuitBreaker(name = "pedidoService", fallbackMethod = "fallbackCrearPagoPorPedidoService")
    @Retry(name = "pedidoService", fallbackMethod = "fallbackCrearPagoPorPedidoService")
    public PaymentResponse create(CreatePaymentRequest request) {
        Map<String, Object> pedido = orderClient.getOrder(request.getPedidoId());
        if (pedido == null || pedido.isEmpty()) {
            throw new EntityNotFoundException("No se encontro el pedido asociado al pago");
        }

        UUID compradorPedido = toUuid(pedido.get("compradorId"));
        BigDecimal totalPedido = toBigDecimal(pedido.get("montoTotal"));
        if (compradorPedido == null || totalPedido == null) {
            throw new IllegalStateException("El pedido no tiene datos consistentes para procesar pago");
        }

        if (request.getCompradorId() != null && !request.getCompradorId().equals(compradorPedido)) {
            throw new IllegalArgumentException("El comprador del pago no coincide con el comprador del pedido");
        }
        if (request.getMonto() != null && request.getMonto().compareTo(totalPedido) != 0) {
            throw new IllegalArgumentException("El monto del pago no coincide con el total del pedido");
        }

        boolean tienePagoActivo = paymentRepository.existsByPedidoIdAndEstadoIn(
                request.getPedidoId(),
                List.of(PaymentStatus.PENDING, PaymentStatus.APPROVED)
        );
        if (tienePagoActivo) {
            throw new IllegalStateException("El pedido ya tiene un pago activo");
        }

        Payment payment = new Payment();
        payment.setPedidoId(request.getPedidoId());
        payment.setCompradorId(compradorPedido);
        payment.setMonto(totalPedido);
        payment.setProveedor(PaymentProvider.MERCADO_PAGO);
        payment.setEstado(PaymentStatus.PENDING);

        String reference = "mp-order-" + request.getPedidoId();
        payment.setReferenciaExterna(reference);
        payment.setPreferenciaId(UUID.randomUUID().toString());
        payment.setUrlCheckout("https://www.mercadopago.com/checkout/v1/redirect?pref_id=" + payment.getPreferenciaId());

        return toResponse(paymentRepository.save(payment));
    }

    public PaymentResponse findById(UUID id) {
        return toResponse(getEntity(id));
    }

    public List<PaymentResponse> findByOrder(UUID orderId) {
        return paymentRepository.findByPedidoId(orderId).stream().map(this::toResponse).toList();
    }

    @CircuitBreaker(name = "pedidoService", fallbackMethod = "fallbackActualizarEstadoPagoPorPedidoService")
    @Retry(name = "pedidoService", fallbackMethod = "fallbackActualizarEstadoPagoPorPedidoService")
    public PaymentResponse updateStatus(UUID id, PaymentStatus status) {
        Payment payment = getEntity(id);
        validarTransicion(payment.getEstado(), status);
        if (payment.getEstado() == status) {
            return toResponse(payment);
        }
        payment.setEstado(status);
        Payment guardado = paymentRepository.save(payment);

        if (status == PaymentStatus.APPROVED) {
            orderClient.updatePaymentStatus(guardado.getPedidoId(), "APPROVED");
            kafkaTemplate.send("pago-aprobado", guardado.getId().toString(), construirEvento(guardado));
        }
        if (status == PaymentStatus.FAILED) {
            orderClient.updatePaymentStatus(guardado.getPedidoId(), "FAILED");
            kafkaTemplate.send("pago-fallido", guardado.getId().toString(), construirEvento(guardado));
        }

        return toResponse(guardado);
    }

    private PaymentResponse fallbackCrearPagoPorPedidoService(CreatePaymentRequest request, Throwable ex) {
        throw new IllegalStateException("No se pudo validar el pedido para crear el pago. Intenta nuevamente.", ex);
    }

    private PaymentResponse fallbackActualizarEstadoPagoPorPedidoService(UUID id, PaymentStatus status, Throwable ex) {
        Payment payment = getEntity(id);
        return toResponse(payment);
    }

    public PaymentResponse procesarWebhookMercadoPago(String externalReference, String status) {
        Payment payment = paymentRepository.findByReferenciaExterna(externalReference)
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
        response.setPedidoId(payment.getPedidoId());
        response.setCompradorId(payment.getCompradorId());
        response.setMonto(payment.getMonto());
        response.setProveedor(payment.getProveedor());
        response.setEstado(payment.getEstado());
        response.setReferenciaExterna(payment.getReferenciaExterna());
        response.setPreferenciaId(payment.getPreferenciaId());
        response.setUrlCheckout(payment.getUrlCheckout());
        response.setCreatedAt(payment.getCreatedAt());
        return response;
    }

    private Map<String, Object> construirEvento(Payment payment) {
        String vendedorId = null;
        try {
            Map<String, Object> pedido = orderClient.getOrder(payment.getPedidoId());
            if (pedido != null && pedido.get("vendedorId") != null) {
                vendedorId = String.valueOf(pedido.get("vendedorId"));
            }
        } catch (Exception ignored) {
        }

        if (vendedorId != null && !vendedorId.isBlank()) {
            return Map.of(
                    "pagoId", payment.getId().toString(),
                    "pedidoId", payment.getPedidoId().toString(),
                    "compradorId", payment.getCompradorId().toString(),
                    "vendedorId", vendedorId,
                    "estado", payment.getEstado().name(),
                    "monto", payment.getMonto()
            );
        }

        return Map.of(
                "pagoId", payment.getId().toString(),
                "pedidoId", payment.getPedidoId().toString(),
                "compradorId", payment.getCompradorId().toString(),
                "estado", payment.getEstado().name(),
                "monto", payment.getMonto()
        );
    }

    private void validarTransicion(PaymentStatus actual, PaymentStatus nuevo) {
        if (actual == PaymentStatus.PENDING) {
            if (nuevo == PaymentStatus.APPROVED || nuevo == PaymentStatus.FAILED || nuevo == PaymentStatus.CANCELLED) {
                return;
            }
        }
        if (actual == PaymentStatus.APPROVED && nuevo == PaymentStatus.REFUNDED) {
            return;
        }
        if (actual == nuevo) {
            return;
        }
        throw new IllegalStateException("Transicion de estado de pago invalida: " + actual + " -> " + nuevo);
    }

    private UUID toUuid(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof UUID uuid) {
            return uuid;
        }
        try {
            return UUID.fromString(Objects.toString(value));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return new BigDecimal(Objects.toString(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
