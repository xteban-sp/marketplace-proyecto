package pe.edu.upeu.product_service.mapper;

import pe.edu.upeu.product_service.dto.response.CategoryResponseDTO;
import pe.edu.upeu.product_service.entity.Category;

public final class CategoryMapper {

    private CategoryMapper() {
    }

    public static CategoryResponseDTO toResponse(Category entity) {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        return dto;
    }
}
