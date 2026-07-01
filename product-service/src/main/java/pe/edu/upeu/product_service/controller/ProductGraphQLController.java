package pe.edu.upeu.product_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import pe.edu.upeu.product_service.dto.response.CategoryResponseDTO;
import pe.edu.upeu.product_service.dto.response.ProductResponseDTO;
import pe.edu.upeu.product_service.service.CategoryService;
import pe.edu.upeu.product_service.service.ProductService;

import java.util.List;
import java.util.UUID;

/**
 * Resolver GraphQL del catálogo. Convive con la API REST reutilizando los
 * mismos servicios. Interfaz interactiva en /graphiql.
 */
@Controller
@RequiredArgsConstructor
public class ProductGraphQLController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @QueryMapping
    public List<ProductResponseDTO> products(@Argument Integer limit) {
        int size = (limit == null || limit <= 0) ? 50 : limit;
        return productService.getAll(PageRequest.of(0, size)).getContent();
    }

    @QueryMapping
    public ProductResponseDTO product(@Argument Long id) {
        return productService.getById(id);
    }

    @QueryMapping
    public List<ProductResponseDTO> productsByCategory(@Argument Long categoryId) {
        return productService.getByCategory(categoryId);
    }

    @QueryMapping
    public List<ProductResponseDTO> productsBySeller(@Argument String sellerId) {
        return productService.getBySeller(UUID.fromString(sellerId));
    }

    @QueryMapping
    public List<CategoryResponseDTO> categories() {
        return categoryService.getAll();
    }

    // El campo sellerId del esquema es String; el DTO lo expone como UUID.
    @SchemaMapping(typeName = "Product", field = "sellerId")
    public String sellerId(ProductResponseDTO p) {
        return p.getSellerId() != null ? p.getSellerId().toString() : null;
    }
}
