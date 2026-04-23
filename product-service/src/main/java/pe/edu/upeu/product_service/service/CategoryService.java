package pe.edu.upeu.product_service.service;

import org.springframework.stereotype.Service;
import pe.edu.upeu.product_service.dto.request.CategoryRequestDTO;
import pe.edu.upeu.product_service.dto.response.CategoryResponseDTO;
import pe.edu.upeu.product_service.entity.Category;
import pe.edu.upeu.product_service.exception.CategoryNotFoundException;
import pe.edu.upeu.product_service.mapper.CategoryMapper;
import pe.edu.upeu.product_service.repository.CategoryRepository;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponseDTO create(CategoryRequestDTO request) {
        Category category = new Category();
        category.setNombre(request.getNombre().trim());
        return CategoryMapper.toResponse(categoryRepository.save(category));
    }

    public List<CategoryResponseDTO> findAll() {
        return categoryRepository.findAll().stream().map(CategoryMapper::toResponse).toList();
    }

    public Category getEntity(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));
    }
}
