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
        if (request.getCompradorId().equals(request.getVendedorId())) {
            throw new IllegalArgumentException("Un usuario no puede comprarse a si mismo");
        }

        Order order = new Order();
        order.setCompradorId(request.getCompradorId());
        order.setVendedorId(request.getVendedorId());
        order.setEstado(OrderStatus.CREATED);
        order.setEstadoPago(PaymentStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            Map<String, Object> producto = productClient.getProduct(itemRequest.getProductoId());
            if (producto == null || producto.isEmpty()) {
                throw new EntityNotFoundException("No se encontro el producto con id: " + itemRequest.getProductoId());
            }

            Number stockDisponible = (Number) producto.get("stock");
            if (stockDisponible != null && itemRequest.getQuantity() > stockDisponible.intValue()) {
                throw new IllegalArgumentException("No hay stock suficiente para el producto " + itemRequest.getProductoId());
            }

            OrderItem item = new OrderItem();
            item.setPedido(order);
            item.setProductoId(itemRequest.getProductoId());
            item.setTituloProducto(itemRequest.getTituloProducto().trim());
            item.setQuantity(itemRequest.getQuantity());
            item.setPrecioUnitario(itemRequest.getPrecioUnitario());
            item.setSubtotal(itemRequest.getPrecioUnitario().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            total = total.add(item.getSubtotal());
            order.getItems().add(item);
        }

        order.setMontoTotal(total);
        Order guardada = orderRepository.save(order);

        Map<String, Object> evento = Map.of(
                "pedidoId", guardada.getId().toString(),
                "compradorId", guardada.getCompradorId().toString(),
                "vendedorId", guardada.getVendedorId().toString(),
                "total", guardada.getMontoTotal()
        );
        kafkaTemplate.send("pedido-creado", guardada.getId().toString(), evento);

        return toResponse(guardada);
    }

    public OrderResponse findById(UUID id) {
        return toResponse(getEntity(id));
    }

    public List<OrderResponse> findAll(UUID compradorId, UUID vendedorId) {
        if (compradorId != null) {
            return orderRepository.findByCompradorId(compradorId).stream().map(this::toResponse).toList();
        }
        if (vendedorId != null) {
            return orderRepository.findByVendedorId(vendedorId).stream().map(this::toResponse).toList();
        }
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }

    public OrderResponse updateStatus(UUID id, OrderStatus status) {
        Order order = getEntity(id);
        order.setEstado(status);
        return toResponse(orderRepository.save(order));
    }

    public OrderResponse updatePaymentStatus(UUID id, PaymentStatus paymentStatus) {
        Order order = getEntity(id);
        order.setEstadoPago(paymentStatus);
        if (paymentStatus == PaymentStatus.APPROVED && order.getEstado() == OrderStatus.CREATED) {
            order.setEstado(OrderStatus.PAID);
        }
        return toResponse(orderRepository.save(order));
    }

    public boolean isReviewEnabled(UUID orderId, UUID userId) {
        Order order = getEntity(orderId);
        return order.getCompradorId().equals(userId) && order.getEstadoPago() == PaymentStatus.APPROVED;
    }

    private Order getEntity(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontro la orden con id: " + id));
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCompradorId(order.getCompradorId());
        response.setVendedorId(order.getVendedorId());
        response.setMontoTotal(order.getMontoTotal());
        response.setEstado(order.getEstado());
        response.setEstadoPago(order.getEstadoPago());
        response.setCreatedAt(order.getCreatedAt());
        response.setItems(order.getItems().stream().map(this::toItemResponse).toList());
        return response;
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setProductoId(item.getProductoId());
        response.setTituloProducto(item.getTituloProducto());
        response.setQuantity(item.getQuantity());
        response.setPrecioUnitario(item.getPrecioUnitario());
        response.setSubtotal(item.getSubtotal());
        return response;
    }
}
