package pe.edu.upeu.recommendation_service.dto;

public class RecommendationResponse {
    private Long productId;
    private Integer score;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
