package pe.edu.upeu.product_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.product_service.dto.request.CategoryRequestDTO;
import pe.edu.upeu.product_service.dto.response.CategoryResponseDTO;
import pe.edu.upeu.product_service.entity.Category;
import pe.edu.upeu.product_service.exception.CategoryNotFoundException;
import pe.edu.upeu.product_service.mapper.CategoryMapper;
import pe.edu.upeu.product_service.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponseDTO create(CategoryRequestDTO dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre: " + dto.getName());
        }
        Category saved = categoryRepository.save(categoryMapper.toEntity(dto));
        return categoryMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO getById(Long id) {                  // ← Long
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Categoría no encontrada: " + id));
        return categoryMapper.toDTO(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryResponseDTO update(Long id, CategoryRequestDTO dto) {  // ← Long
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Categoría no encontrada: " + id));
        categoryMapper.updateEntity(category, dto);
        return categoryMapper.toDTO(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long id) {                                  // ← Long
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException("Categoría no encontrada: " + id);
        }
        categoryRepository.deleteById(id);
    }
}