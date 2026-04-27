package pe.edu.upeu.product_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.product_service.dto.request.ProductRequestDTO;
import pe.edu.upeu.product_service.dto.response.ProductResponseDTO;
import pe.edu.upeu.product_service.entity.Category;
import pe.edu.upeu.product_service.entity.Product;
import pe.edu.upeu.product_service.exception.CategoryNotFoundException;
import pe.edu.upeu.product_service.exception.ProductNotFoundException;
import pe.edu.upeu.product_service.repository.CategoryRepository;
import pe.edu.upeu.product_service.repository.ProductRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponseDTO create(ProductRequestDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Categoría no encontrada: " + dto.getCategoryId()));
        Product saved = productRepository.save(toEntity(dto, category));
        log.info("Producto creado con ID: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductResponseDTO getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
// ← sin @Cacheable, Page<T> no es serializable en Redis
    public Page<ProductResponseDTO> getAll(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> search(String name, Long categoryId, Pageable pageable) {
        return productRepository.searchProducts(name, categoryId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getBySeller(Long sellerId) {
        return productRepository.findBySellerIdAndActiveTrue(sellerId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    @CachePut(value = "products", key = "#id")
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponseDTO update(Long id, ProductRequestDTO dto) {
        Product product = getEntity(id);
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Categoría no encontrada: " + dto.getCategoryId()));
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setImageUrl(dto.getImageUrl());
        product.setSellerId(dto.getSellerId());
        product.setCategory(category);
        log.info("Producto actualizado con ID: {}", id);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void delete(Long id) {
        Product product = getEntity(id);
        product.setActive(false);
        productRepository.save(product);
        log.info("Producto desactivado (soft delete) con ID: {}", id);
    }

    // ── helpers privados ──────────────────────────────────────────────────────

    private Product getEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado: " + id));
    }

    private Product toEntity(ProductRequestDTO dto, Category category) {
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

    private ProductResponseDTO toResponse(Product p) {
        return ProductResponseDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .stock(p.getStock())
                .imageUrl(p.getImageUrl())
                .active(p.getActive())
                .sellerId(p.getSellerId())
                .categoryId(p.getCategory().getId())
                .categoryName(p.getCategory().getName())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}