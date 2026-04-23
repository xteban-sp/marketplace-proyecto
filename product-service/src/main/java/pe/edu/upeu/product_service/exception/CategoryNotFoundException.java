package pe.edu.upeu.product_service.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(Long categoryId) {
        super("Categoria no encontrada: " + categoryId);
    }
}
