package pe.edu.upeu.recommendation_service.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.edu.upeu.recommendation_service.dto.CreateRecommendationRequest;
import pe.edu.upeu.recommendation_service.entity.RecommendationSource;

import java.util.Map;
import java.util.UUID;

@Component
public class RecommendationEventListener {

    private final RecommendationService recommendationService;

    public RecommendationEventListener(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @KafkaListener(topics = "resena-creada", groupId = "recommendation-service")
    public void onResenaCreada(Map<String, Object> payload) {
        CreateRecommendationRequest request = new CreateRecommendationRequest();
        request.setUsuarioId(UUID.fromString(String.valueOf(payload.get("usuarioId"))));
        request.setProductoId(Long.valueOf(String.valueOf(payload.get("productoId"))));

        double puntuacion = Double.parseDouble(String.valueOf(payload.get("puntuacion")));
        request.setPuntaje(puntuacion * 20.0);
        request.setFuente(RecommendationSource.PURCHASE_HISTORY);

        recommendationService.upsert(request);
    }
}
