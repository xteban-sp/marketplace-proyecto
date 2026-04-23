package pe.edu.upeu.review_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.review_service.dto.CreateReviewRequest;
import pe.edu.upeu.review_service.dto.ReviewResponse;
import pe.edu.upeu.review_service.service.ReviewService;
import java.util.List;

@RestController
@RequestMapping("/api/resenas")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@Valid @RequestBody CreateReviewRequest request) {
        return reviewService.create(request);
    }

    @GetMapping
    public List<ReviewResponse> byProduct(@RequestParam(required = false) Long productId,
                                          @RequestParam(required = false) Long productoId) {
        Long objetivo = productoId != null ? productoId : productId;
        if (objetivo == null) {
            throw new IllegalArgumentException("Debe enviar productId o productoId");
        }
        return reviewService.byProduct(objetivo);
    }
}
