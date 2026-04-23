package pe.edu.upeu.payment_service.dto;

import pe.edu.upeu.payment_service.entity.PaymentProvider;
import pe.edu.upeu.payment_service.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentResponse {

    private UUID id;
    private UUID pedidoId;
    private UUID compradorId;
    private BigDecimal monto;
    private PaymentProvider proveedor;
    private PaymentStatus estado;
    private String referenciaExterna;
    private String preferenciaId;
    private String urlCheckout;
    private LocalDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(UUID pedidoId) {
        this.pedidoId = pedidoId;
    }

    public UUID getCompradorId() {
        return compradorId;
    }

    public void setCompradorId(UUID compradorId) {
        this.compradorId = compradorId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public PaymentProvider getProveedor() {
        return proveedor;
    }

    public void setProveedor(PaymentProvider proveedor) {
        this.proveedor = proveedor;
    }

    public PaymentStatus getEstado() {
        return estado;
    }

    public void setEstado(PaymentStatus estado) {
        this.estado = estado;
    }

    public String getReferenciaExterna() {
        return referenciaExterna;
    }

    public void setReferenciaExterna(String referenciaExterna) {
        this.referenciaExterna = referenciaExterna;
    }

    public String getPreferenciaId() {
        return preferenciaId;
    }

    public void setPreferenciaId(String preferenciaId) {
        this.preferenciaId = preferenciaId;
    }

    public String getUrlCheckout() {
        return urlCheckout;
    }

    public void setUrlCheckout(String urlCheckout) {
        this.urlCheckout = urlCheckout;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
