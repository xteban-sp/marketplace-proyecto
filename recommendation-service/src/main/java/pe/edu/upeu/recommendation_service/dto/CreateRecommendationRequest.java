package pe.edu.upeu.recommendation_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import pe.edu.upeu.recommendation_service.entity.RecommendationSource;

import java.util.UUID;

public class CreateRecommendationRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private Long productId;

    @NotNull
    @Min(0)
    @Max(100)
    private Double score;

    @NotNull
    private RecommendationSource source;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public RecommendationSource getSource() {
        return source;
    }

    public void setSource(RecommendationSource source) {
        this.source = source;
    }
}
