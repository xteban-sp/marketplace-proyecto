package pe.edu.upeu.order_service.dto;

import pe.edu.upeu.order_service.entity.OrderStatus;
import pe.edu.upeu.order_service.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OrderResponse {

    private UUID id;
    private UUID compradorId;
    private UUID vendedorId;
    private BigDecimal montoTotal;
    private OrderStatus estado;
    private PaymentStatus estadoPago;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCompradorId() {
        return compradorId;
    }

    public void setCompradorId(UUID compradorId) {
        this.compradorId = compradorId;
    }

    public UUID getVendedorId() {
        return vendedorId;
    }

    public void setVendedorId(UUID vendedorId) {
        this.vendedorId = vendedorId;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public OrderStatus getEstado() {
        return estado;
    }

    public void setEstado(OrderStatus estado) {
        this.estado = estado;
    }

    public PaymentStatus getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(PaymentStatus estadoPago) {
        this.estadoPago = estadoPago;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponse> items) {
        this.items = items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
