package pe.edu.upeu.review_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.review_service.model.ReviewEntity;
import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    List<ReviewEntity> findByProductoIdOrderByCreatedAtDesc(Long productoId);
    boolean existsByPedidoIdAndUsuarioId(UUID pedidoId, UUID usuarioId);
}
