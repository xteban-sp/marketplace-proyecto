package pe.edu.upeu.review_service.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;
import pe.edu.upeu.review_service.client.OrderClient;
import pe.edu.upeu.review_service.dto.CreateReviewRequest;
import pe.edu.upeu.review_service.dto.ReviewResponse;
import pe.edu.upeu.review_service.model.ReviewEntity;
import pe.edu.upeu.review_service.repository.ReviewRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderClient orderClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ReviewService(ReviewRepository reviewRepository,
                         OrderClient orderClient,
                         KafkaTemplate<String, Object> kafkaTemplate) {
        this.reviewRepository = reviewRepository;
        this.orderClient = orderClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @CircuitBreaker(name = "pedidoService", fallbackMethod = "fallbackValidarResenaPorPedidoService")
    @Retry(name = "pedidoService", fallbackMethod = "fallbackValidarResenaPorPedidoService")
    public ReviewResponse create(CreateReviewRequest request) {
        if (reviewRepository.existsByPedidoIdAndUsuarioId(request.getPedidoId(), request.getUsuarioId())) {
            throw new IllegalArgumentException("Ya existe una resena para esta orden y usuario");
        }

        Map<String, Boolean> eligibility = orderClient.isReviewEligible(request.getPedidoId(), request.getUsuarioId());
        boolean eligible = Boolean.TRUE.equals(eligibility.get("eligible"));
        if (!eligible) {
            throw new IllegalArgumentException("El usuario no puede resenar esta orden");
        }

        ReviewEntity entity = new ReviewEntity();
        entity.setPedidoId(request.getPedidoId());
        entity.setProductoId(request.getProductoId());
        entity.setUsuarioId(request.getUsuarioId());
        entity.setPuntuacion(request.getPuntuacion());
        entity.setComentario(request.getComentario().trim());

        ReviewEntity guardada = reviewRepository.save(entity);
        Map<String, Object> evento = Map.of(
                "resenaId", guardada.getId(),
                "pedidoId", guardada.getPedidoId().toString(),
                "productoId", guardada.getProductoId(),
                "usuarioId", guardada.getUsuarioId().toString(),
                "puntuacion", guardada.getPuntuacion()
        );
        kafkaTemplate.send("resena-creada", guardada.getId().toString(), evento);

        return toResponse(guardada);
    }

    private ReviewResponse fallbackValidarResenaPorPedidoService(CreateReviewRequest request, Throwable ex) {
        throw new IllegalStateException("No se pudo validar la elegibilidad de resena en este momento. Intenta nuevamente.", ex);
    }

    public List<ReviewResponse> byProduct(Long productId) {
        return reviewRepository.findByProductoIdOrderByCreatedAtDesc(productId).stream().map(this::toResponse).toList();
    }

    private ReviewResponse toResponse(ReviewEntity entity) {
        ReviewResponse dto = new ReviewResponse();
        dto.setId(entity.getId());
        dto.setPedidoId(entity.getPedidoId());
        dto.setProductoId(entity.getProductoId());
        dto.setUsuarioId(entity.getUsuarioId());
        dto.setPuntuacion(entity.getPuntuacion());
        dto.setComentario(entity.getComentario());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
