package pe.edu.upeu.auth_service.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String message;
        private Object details;
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, Object details) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(LocalDateTime.now(), status.value(), message, details));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException ex) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("path", ex.getRequestURL());
        details.put("method", ex.getHttpMethod());
        return error(HttpStatus.NOT_FOUND, "La ruta solicitada no existe.", details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        Map<String, String> details = new LinkedHashMap<>();
        String cause = (ex.getMostSpecificCause() != null) ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        details.put("cause", cause != null ? cause : "Cuerpo de solicitud inválido");
        return error(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud está malformado.", details);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "Error de validación", errors);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        Map<String, String> details = new LinkedHashMap<>();
        String cause = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        details.put("hint", "Verifique unicidad o restricciones de la base de datos");
        details.put("cause", cause != null ? cause : "Violación de integridad");
        return error(HttpStatus.CONFLICT, "La operación viola restricciones de la base de datos.", details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("message", ex.getMessage() != null ? ex.getMessage() : "Error desconocido");
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", details);
    }
}