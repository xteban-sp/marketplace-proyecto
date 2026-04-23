package pe.edu.upeu.review_service.service;

import org.springframework.stereotype.Service;
import pe.edu.upeu.review_service.client.OrderClient;
import pe.edu.upeu.review_service.dto.CreateReviewRequest;
import pe.edu.upeu.review_service.dto.ReviewResponse;
import pe.edu.upeu.review_service.model.ReviewEntity;
import pe.edu.upeu.review_service.repository.ReviewRepository;
import java.util.List;
import java.util.Map;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderClient orderClient;

    public ReviewService(ReviewRepository reviewRepository, OrderClient orderClient) {
        this.reviewRepository = reviewRepository;
        this.orderClient = orderClient;
    }

    public ReviewResponse create(CreateReviewRequest request) {
        if (reviewRepository.existsByOrderIdAndUsername(request.getOrderId(), request.getUsername())) {
            throw new IllegalArgumentException("Ya existe una resena para esta orden y usuario");
        }

        Map<String, Boolean> eligibility = orderClient.isReviewEligible(request.getOrderId(), request.getUsername());
        boolean eligible = Boolean.TRUE.equals(eligibility.get("eligible"));
        if (!eligible) {
            throw new IllegalArgumentException("El usuario no puede resenar esta orden");
        }

        ReviewEntity entity = new ReviewEntity();
        entity.setOrderId(request.getOrderId());
        entity.setProductId(request.getProductId());
        entity.setUsername(request.getUsername());
        entity.setRating(request.getRating());
        entity.setComment(request.getComment().trim());

        return toResponse(reviewRepository.save(entity));
    }

    public List<ReviewResponse> byProduct(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId).stream().map(this::toResponse).toList();
    }

    private ReviewResponse toResponse(ReviewEntity entity) {
        ReviewResponse dto = new ReviewResponse();
        dto.setId(entity.getId());
        dto.setOrderId(entity.getOrderId());
        dto.setProductId(entity.getProductId());
        dto.setUsername(entity.getUsername());
        dto.setRating(entity.getRating());
        dto.setComment(entity.getComment());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
