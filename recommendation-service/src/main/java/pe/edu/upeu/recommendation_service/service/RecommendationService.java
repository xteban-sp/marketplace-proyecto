package pe.edu.upeu.recommendation_service.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import pe.edu.upeu.recommendation_service.dto.CreateRecommendationRequest;
import pe.edu.upeu.recommendation_service.dto.RecommendationResponse;
import pe.edu.upeu.recommendation_service.entity.Recommendation;
import pe.edu.upeu.recommendation_service.repository.RecommendationRepository;

import java.util.List;
import java.util.UUID;

@Service
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;

    public RecommendationService(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    @CacheEvict(value = "recommendations", key = "#request.userId")
    public RecommendationResponse upsert(CreateRecommendationRequest request) {
        Recommendation recommendation = recommendationRepository
                .findByUserIdAndProductId(request.getUserId(), request.getProductId())
                .orElseGet(Recommendation::new);

        recommendation.setUserId(request.getUserId());
        recommendation.setProductId(request.getProductId());
        recommendation.setScore(request.getScore());
        recommendation.setSource(request.getSource());
        return toResponse(recommendationRepository.save(recommendation));
    }

    @Cacheable(value = "recommendations", key = "#userId")
    public List<RecommendationResponse> listByUser(UUID userId) {
        return recommendationRepository.findTop20ByUserIdOrderByScoreDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private RecommendationResponse toResponse(Recommendation recommendation) {
        RecommendationResponse response = new RecommendationResponse();
        response.setId(recommendation.getId());
        response.setUserId(recommendation.getUserId());
        response.setProductId(recommendation.getProductId());
        response.setScore(recommendation.getScore());
        response.setSource(recommendation.getSource());
        response.setUpdatedAt(recommendation.getUpdatedAt());
        return response;
    }
}
