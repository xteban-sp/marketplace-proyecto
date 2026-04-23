package pe.edu.upeu.product_service.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import pe.edu.upeu.product_service.dto.request.ProductRequestDTO;
import pe.edu.upeu.product_service.dto.response.ProductResponseDTO;
import pe.edu.upeu.product_service.entity.Product;
import pe.edu.upeu.product_service.exception.ProductNotFoundException;
import pe.edu.upeu.product_service.mapper.ProductMapper;
import pe.edu.upeu.product_service.repository.ProductRepository;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    @CacheEvict(value = "products", allEntries = true)
    public ProductResponseDTO create(ProductRequestDTO request) {
        Product entity = new Product();
        applyRequest(entity, request);
        return ProductMapper.toResponse(productRepository.save(entity));
    }

    @Cacheable("products")
    public List<ProductResponseDTO> findAll() {
        return productRepository.findAll().stream().map(ProductMapper::toResponse).toList();
    }

    public ProductResponseDTO findById(Long id) {
        return ProductMapper.toResponse(getEntity(id));
    }

    @CacheEvict(value = "products", allEntries = true)
    public ProductResponseDTO update(Long id, ProductRequestDTO request) {
        Product current = getEntity(id);
        applyRequest(current, request);
        return ProductMapper.toResponse(productRepository.save(current));
    }

    @CacheEvict(value = "products", allEntries = true)
    public void delete(Long id) {
        productRepository.delete(getEntity(id));
    }

    public List<ProductResponseDTO> search(String query) {
        return productRepository.findByTituloContainingIgnoreCase(query).stream().map(ProductMapper::toResponse).toList();
    }

    public List<ProductResponseDTO> findByCategory(Long categoriaId) {
        return productRepository.findByCategoriaId(categoriaId).stream().map(ProductMapper::toResponse).toList();
    }

    private Product getEntity(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    private void applyRequest(Product entity, ProductRequestDTO request) {
        entity.setTitulo(request.getTitulo().trim());
        entity.setDescripcion(request.getDescripcion().trim());
        entity.setPrecio(request.getPrecio());
        entity.setStock(request.getStock());
        entity.setUsuarioVendedor(request.getUsuarioVendedor().trim());
        entity.setCategoria(categoryService.getEntity(request.getCategoriaId()));
    }
}
