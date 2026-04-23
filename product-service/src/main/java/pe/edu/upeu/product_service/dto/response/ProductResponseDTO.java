package pe.edu.upeu.product_service.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private Boolean active;

    // Del vendedor — se enriquece con Feign desde user-service
    private Long sellerId;
    private String sellerName;     // Nombre del vendedor (viene del UserClient)

    // Categoría embebida en la respuesta
    private Long categoryId;
    private String categoryName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}