package pe.edu.upeu.product_service.service;

import pe.edu.upeu.product_service.dto.request.CategoryRequestDTO;
import pe.edu.upeu.product_service.dto.response.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {
    CategoryResponseDTO create(CategoryRequestDTO dto);
    CategoryResponseDTO getById(Long id);                          // ← Long
    List<CategoryResponseDTO> getAll();
    CategoryResponseDTO update(Long id, CategoryRequestDTO dto);   // ← Long
    void delete(Long id);                                          // ← Long
}