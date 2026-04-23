package pe.edu.upeu.product_service.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long productId) {
        super("Producto no encontrado: " + productId);
    }
}
