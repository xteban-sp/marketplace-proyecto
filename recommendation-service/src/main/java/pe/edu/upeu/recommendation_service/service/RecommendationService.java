package pe.edu.upeu.recommendation_service.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import pe.edu.upeu.recommendation_service.dto.RecommendationResponse;
import pe.edu.upeu.recommendation_service.model.RecommendationEntity;
import pe.edu.upeu.recommendation_service.repository.RecommendationRepository;
import java.util.List;
import java.util.Map;

@Service
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;

    public RecommendationService(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    @Cacheable(value = "recommendations", key = "#username")
    public List<RecommendationResponse> byUser(String username) {
        return recommendationRepository.findByUsernameOrderByScoreDesc(username)
                .stream()
                .limit(20)
                .map(this::toResponse)
                .toList();
    }

    @KafkaListener(topics = "payment-approved", groupId = "recommendation-service")
    @CacheEvict(value = "recommendations", key = "#event['buyerUsername']")
    public void onPaymentApproved(Map<String, Object> event) {
        Object usernameObj = event.get("buyerUsername");
        Object productObj = event.get("productId");

        if (usernameObj == null || productObj == null) {
            return;
        }

        String username = usernameObj.toString();
        Long productId = Long.valueOf(productObj.toString());

        RecommendationEntity current = recommendationRepository
                .findByUsernameAndProductId(username, productId)
                .orElseGet(() -> {
                    RecommendationEntity e = new RecommendationEntity();
                    e.setUsername(username);
                    e.setProductId(productId);
                    e.setScore(0);
                    return e;
                });

        current.setScore(current.getScore() + 1);
        recommendationRepository.save(current);
    }

    private RecommendationResponse toResponse(RecommendationEntity entity) {
        RecommendationResponse response = new RecommendationResponse();
        response.setProductId(entity.getProductId());
        response.setScore(entity.getScore());
        return response;
    }
}
