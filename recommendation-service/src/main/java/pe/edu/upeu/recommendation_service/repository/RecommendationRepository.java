package pe.edu.upeu.recommendation_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.recommendation_service.entity.Recommendation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecommendationRepository extends JpaRepository<Recommendation, UUID> {
    List<Recommendation> findTop20ByUsuarioIdOrderByPuntajeDesc(UUID usuarioId);
    Optional<Recommendation> findByUsuarioIdAndProductoId(UUID usuarioId, Long productoId);
}
