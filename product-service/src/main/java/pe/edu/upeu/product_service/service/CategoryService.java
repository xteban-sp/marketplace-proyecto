package pe.edu.upeu.product_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.product_service.dto.request.CategoryRequestDTO;
import pe.edu.upeu.product_service.dto.response.CategoryResponseDTO;
import pe.edu.upeu.product_service.entity.Category;
import pe.edu.upeu.product_service.exception.CategoryNotFoundException;
import pe.edu.upeu.product_service.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponseDTO create(CategoryRequestDTO dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre: " + dto.getName());
        }
        return toResponse(categoryRepository.save(toEntity(dto)));
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAll() {
        return categoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public CategoryResponseDTO update(Long id, CategoryRequestDTO dto) {
        Category category = getEntity(id);
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException("Categoría no encontrada: " + id);
        }
        categoryRepository.deleteById(id);
    }

    // ── helpers privados ──────────────────────────────────────────────────────

    private Category getEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Categoría no encontrada: " + id));
    }

    private Category toEntity(CategoryRequestDTO dto) {
        return Category.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
    }

    private CategoryResponseDTO toResponse(Category c) {
        return CategoryResponseDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .build();
    }
}