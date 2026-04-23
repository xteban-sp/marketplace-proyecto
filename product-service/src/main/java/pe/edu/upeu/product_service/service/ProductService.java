package pe.edu.upeu.product_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.edu.upeu.product_service.dto.request.ProductRequestDTO;
import pe.edu.upeu.product_service.dto.response.ProductResponseDTO;

import java.util.List;

public interface ProductService {

    ProductResponseDTO create(ProductRequestDTO dto);
    ProductResponseDTO getById(Long id);
    Page<ProductResponseDTO> getAll(Pageable pageable);
    Page<ProductResponseDTO> search(String name, Long categoryId, Pageable pageable);
    List<ProductResponseDTO> getBySeller(Long sellerId);
    List<ProductResponseDTO> getByCategory(Long categoryId);
    ProductResponseDTO update(Long id, ProductRequestDTO dto);
    void delete(Long id);         // Soft delete
}