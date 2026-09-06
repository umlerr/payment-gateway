package umlerr.servicepayment.handler;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import umlerr.servicepayment.dto.ErrorResponse;
import umlerr.servicepayment.exception.ConflictException;
import umlerr.servicepayment.exception.NotFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException e, ServletWebRequest request) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", e.getMessage(), request, null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException e, ServletWebRequest request) {
        return build(HttpStatus.CONFLICT, "CONFLICT", e.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
        MethodArgumentNotValidException e, ServletWebRequest request) {
        Map<String, String> details = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
            .forEach(error -> details.put(error.getField(), error.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "validation failed", request, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException e, ServletWebRequest request) {
        Map<String, String> details = new LinkedHashMap<>();
        e.getConstraintViolations()
            .forEach(violation -> details.put(violation.getPropertyPath().toString(), violation.getMessage()));
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "validation failed", request, details);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message,
                                                ServletWebRequest request, Map<String, String> details) {
        ErrorResponse body = ErrorResponse.builder()
            .code(code)
            .message(message)
            .path(request.getRequest().getRequestURI())
            .timestamp(java.time.LocalDateTime.now())
            .details(details)
            .build();
        return ResponseEntity.status(status).body(body);
    }
}
