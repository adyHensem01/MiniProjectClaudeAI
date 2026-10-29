package wedding.com.example.weddingPackage.exception;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import wedding.com.example.weddingPackage.dto.ApiError;

/**
 * Turns every failure into a controlled JSON response instead of a crash or stack trace.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Bad user input: failed @Valid checks. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Request validation failed", details);
    }

    /** Bad user input: malformed JSON or wrong types (e.g. "guestCount": "lots"). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Request body is missing or is not valid JSON", List.of());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    /** Claude answered but the answer failed validation. */
    @ExceptionHandler(InvalidAiOutputException.class)
    public ResponseEntity<ApiError> handleInvalidAi(InvalidAiOutputException ex) {
        log.warn("Rejected AI output: {} {}", ex.getMessage(), ex.getProblems());
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), ex.getProblems());
    }

    /** Claude unreachable / rate limited / misconfigured. */
    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<ApiError> handleAiService(AiServiceException ex) {
        log.error("Claude API failure: {}", ex.getMessage(), ex.getCause());
        return build(ex.getStatus(), ex.getMessage(), List.of());
    }

    /** MongoDB down or query failed. */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDatabase(DataAccessException ex) {
        log.error("Database failure", ex);
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Database is temporarily unavailable", List.of());
    }

    /** Last resort - never leak internals. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", List.of());
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, List<String> details) {
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(), message, details));
    }
}
