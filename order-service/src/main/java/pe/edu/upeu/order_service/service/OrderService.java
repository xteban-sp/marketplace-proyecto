package pe.edu.upeu.order_service.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pe.edu.upeu.order_service.client.ProductClient;
import pe.edu.upeu.order_service.dto.CreateOrderRequest;
import pe.edu.upeu.order_service.dto.OrderItemRequest;
import pe.edu.upeu.order_service.dto.OrderItemResponse;
import pe.edu.upeu.order_service.dto.OrderResponse;
import pe.edu.upeu.order_service.entity.Order;
import pe.edu.upeu.order_service.entity.OrderItem;
import pe.edu.upeu.order_service.entity.OrderStatus;
import pe.edu.upeu.order_service.entity.PaymentStatus;
import pe.edu.upeu.order_service.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderService(OrderRepository orderRepository,
                        ProductClient productClient,
                        KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public OrderResponse create(CreateOrderRequest request) {
        if (request.getBuyerId().equals(request.getSellerId())) {
            throw new IllegalArgumentException("Un usuario no puede comprarse a si mismo");
        }

        Order order = new Order();
        order.setBuyerId(request.getBuyerId());
        order.setSellerId(request.getSellerId());
        order.setStatus(OrderStatus.CREATED);
        order.setPaymentStatus(PaymentStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            Map<String, Object> producto = productClient.getProduct(itemRequest.getProductId());
            if (producto == null || producto.isEmpty()) {
                throw new EntityNotFoundException("No se encontro el producto con id: " + itemRequest.getProductId());
            }

            Number stockDisponible = (Number) producto.get("stock");
            if (stockDisponible != null && itemRequest.getQuantity() > stockDisponible.intValue()) {
                throw new IllegalArgumentException("No hay stock suficiente para el producto " + itemRequest.getProductId());
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(itemRequest.getProductId());
            item.setProductTitle(itemRequest.getProductTitle().trim());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(itemRequest.getUnitPrice());
            item.setSubtotal(itemRequest.getUnitPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            total = total.add(item.getSubtotal());
            order.getItems().add(item);
        }

        order.setTotalAmount(total);
        Order guardada = orderRepository.save(order);

        Map<String, Object> evento = Map.of(
                "pedidoId", guardada.getId().toString(),
                "compradorId", guardada.getBuyerId().toString(),
                "vendedorId", guardada.getSellerId().toString(),
                "total", guardada.getTotalAmount()
        );
        kafkaTemplate.send("pedido-creado", guardada.getId().toString(), evento);

        return toResponse(guardada);
    }

    public OrderResponse findById(UUID id) {
        return toResponse(getEntity(id));
    }

    public List<OrderResponse> findAll(UUID buyerId, UUID sellerId) {
        if (buyerId != null) {
            return orderRepository.findByBuyerId(buyerId).stream().map(this::toResponse).toList();
        }
        if (sellerId != null) {
            return orderRepository.findBySellerId(sellerId).stream().map(this::toResponse).toList();
        }
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }

    public OrderResponse updateStatus(UUID id, OrderStatus status) {
        Order order = getEntity(id);
        order.setStatus(status);
        return toResponse(orderRepository.save(order));
    }

    public OrderResponse updatePaymentStatus(UUID id, PaymentStatus paymentStatus) {
        Order order = getEntity(id);
        order.setPaymentStatus(paymentStatus);
        if (paymentStatus == PaymentStatus.APPROVED && order.getStatus() == OrderStatus.CREATED) {
            order.setStatus(OrderStatus.PAID);
        }
        return toResponse(orderRepository.save(order));
    }

    public boolean isReviewEnabled(UUID orderId, UUID userId) {
        Order order = getEntity(orderId);
        return order.getBuyerId().equals(userId) && order.getPaymentStatus() == PaymentStatus.APPROVED;
    }

    private Order getEntity(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontro la orden con id: " + id));
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setBuyerId(order.getBuyerId());
        response.setSellerId(order.getSellerId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setItems(order.getItems().stream().map(this::toItemResponse).toList());
        return response;
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setProductId(item.getProductId());
        response.setProductTitle(item.getProductTitle());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setSubtotal(item.getSubtotal());
        return response;
    }
}
