package pe.edu.upeu.recommendation_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.recommendation_service.dto.CreateRecommendationRequest;
import pe.edu.upeu.recommendation_service.dto.RecommendationResponse;
import pe.edu.upeu.recommendation_service.service.RecommendationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/recomendaciones")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecommendationResponse upsert(@Valid @RequestBody CreateRecommendationRequest request) {
        return recommendationService.upsert(request);
    }

    @GetMapping
    public List<RecommendationResponse> listByUser(@RequestParam UUID usuarioId) {
        return recommendationService.listByUser(usuarioId);
    }
}
