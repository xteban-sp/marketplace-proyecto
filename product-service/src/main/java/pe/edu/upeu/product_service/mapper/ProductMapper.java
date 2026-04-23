package pe.edu.upeu.product_service.mapper;

import pe.edu.upeu.product_service.dto.response.ProductResponseDTO;
import pe.edu.upeu.product_service.entity.Product;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static ProductResponseDTO toResponse(Product entity) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(entity.getId());
        dto.setTitulo(entity.getTitulo());
        dto.setDescripcion(entity.getDescripcion());
        dto.setPrecio(entity.getPrecio());
        dto.setStock(entity.getStock());
        dto.setUsuarioVendedor(entity.getUsuarioVendedor());
        dto.setCategoriaId(entity.getCategoria().getId());
        dto.setCategoriaNombre(entity.getCategoria().getNombre());
        return dto;
    }
}
