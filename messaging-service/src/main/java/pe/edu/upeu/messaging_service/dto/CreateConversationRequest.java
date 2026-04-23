package pe.edu.upeu.messaging_service.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CreateConversationRequest {

    @NotNull
    private UUID buyerId;

    @NotNull
    private UUID sellerId;

    @NotNull
    private Long productId;

    public UUID getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(UUID buyerId) {
        this.buyerId = buyerId;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public void setSellerId(UUID sellerId) {
        this.sellerId = sellerId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
