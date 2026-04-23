package pe.edu.upeu.review_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.review_service.model.ReviewEntity;
import java.util.List;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    List<ReviewEntity> findByProductIdOrderByCreatedAtDesc(Long productId);
    boolean existsByOrderIdAndUsername(Long orderId, String username);
}
