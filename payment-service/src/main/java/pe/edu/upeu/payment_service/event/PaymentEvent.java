package pe.edu.upeu.payment_service.event;

public class PaymentEvent {
    private Long paymentId;
    private Long orderId;
    private Long productId;
    private String buyerUsername;
    private String status;

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getBuyerUsername() { return buyerUsername; }
    public void setBuyerUsername(String buyerUsername) { this.buyerUsername = buyerUsername; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
