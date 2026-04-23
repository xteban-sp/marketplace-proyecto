package pe.edu.upeu.product_service.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.product_service.dto.request.ProductRequestDTO;
import pe.edu.upeu.product_service.dto.response.ProductResponseDTO;
import pe.edu.upeu.product_service.entity.Category;
import pe.edu.upeu.product_service.entity.Product;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequestDTO dto, Category category) {
        return Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .imageUrl(dto.getImageUrl())
                .active(true)
                .sellerId(dto.getSellerId())
                .category(category)
                .build();
    }

    public ProductResponseDTO toDTO(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .active(product.getActive())
                .sellerId(product.getSellerId())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public void updateEntity(Product product, ProductRequestDTO dto, Category category) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setImageUrl(dto.getImageUrl());
        product.setSellerId(dto.getSellerId());
        product.setCategory(category);
    }
}