package pe.edu.upeu.payment_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pagos")
public class Payment {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID pedidoId;

    @Column(nullable = false)
    private UUID compradorId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentProvider proveedor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus estado;

    @Column(length = 120)
    private String referenciaExterna;

    @Column(length = 120)
    private String preferenciaId;

    @Column(length = 300)
    private String urlCheckout;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (proveedor == null) {
            proveedor = PaymentProvider.MERCADO_PAGO;
        }
        if (estado == null) {
            estado = PaymentStatus.PENDING;
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
