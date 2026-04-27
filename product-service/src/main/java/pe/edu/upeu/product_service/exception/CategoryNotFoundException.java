package pe.edu.upeu.product_service.exception;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(String message) {   // ← String, no Long
        super(message);
    }
}