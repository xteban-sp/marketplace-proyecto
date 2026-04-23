package pe.edu.upeu.recommendation_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.recommendation_service.model.RecommendationEntity;
import java.util.List;
import java.util.Optional;

public interface RecommendationRepository extends JpaRepository<RecommendationEntity, Long> {
    List<RecommendationEntity> findByUsernameOrderByScoreDesc(String username);
    Optional<RecommendationEntity> findByUsernameAndProductId(String username, Long productId);
}
