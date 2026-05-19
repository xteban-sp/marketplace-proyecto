package pe.edu.upeu.auth_service.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
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

    // FIX: RuntimeException con mensaje "no encontrado" → 404; resto → 500
    // Antes toda RuntimeException causaba un HTTP 500 genérico sin mensaje útil
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
        String message = ex.getMessage();
        if (message != null && (message.toLowerCase().contains("no encontrado") ||
                                message.toLowerCase().contains("not found"))) {
            return error(HttpStatus.NOT_FOUND, message, null);
        }
        Map<String, String> details = new LinkedHashMap<>();
        details.put("message", message != null ? message : "Error desconocido");
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", details);
    }

    // FIX: 401 para fallos de autenticación de Spring Security (no 500)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        return error(HttpStatus.UNAUTHORIZED, "No autenticado: " + ex.getMessage(), null);
    }

    // FIX: 403 para accesos denegados por roles (@PreAuthorize)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return error(HttpStatus.FORBIDDEN, "Acceso denegado: no tienes permisos para realizar esta acción", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("message", ex.getMessage() != null ? ex.getMessage() : "Error desconocido");
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", details);
    }
}
