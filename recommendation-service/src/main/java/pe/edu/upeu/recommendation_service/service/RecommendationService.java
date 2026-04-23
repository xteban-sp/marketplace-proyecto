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

    @CacheEvict(value = "recommendations", key = "#request.usuarioId")
    public RecommendationResponse upsert(CreateRecommendationRequest request) {
        Recommendation recommendation = recommendationRepository
                .findByUsuarioIdAndProductoId(request.getUsuarioId(), request.getProductoId())
                .orElseGet(Recommendation::new);

        recommendation.setUsuarioId(request.getUsuarioId());
        recommendation.setProductoId(request.getProductoId());
        recommendation.setPuntaje(request.getPuntaje());
        recommendation.setFuente(request.getFuente());
        return toResponse(recommendationRepository.save(recommendation));
    }

    @Cacheable(value = "recommendations", key = "#usuarioId")
    public List<RecommendationResponse> listByUser(UUID usuarioId) {
        return recommendationRepository.findTop20ByUsuarioIdOrderByPuntajeDesc(usuarioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private RecommendationResponse toResponse(Recommendation recommendation) {
        RecommendationResponse response = new RecommendationResponse();
        response.setId(recommendation.getId());
        response.setUsuarioId(recommendation.getUsuarioId());
        response.setProductoId(recommendation.getProductoId());
        response.setPuntaje(recommendation.getPuntaje());
        response.setFuente(recommendation.getFuente());
        response.setUpdatedAt(recommendation.getUpdatedAt());
        return response;
    }
}
