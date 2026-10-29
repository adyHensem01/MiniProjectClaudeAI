package wedding.com.example.weddingPackage.dto;

import java.time.Instant;
import java.util.List;

/**
 * Controlled error body returned for every failure - never a stack trace.
 */
public record ApiError(int status, String error, String message, List<String> details, Instant timestamp) {

    public static ApiError of(int status, String error, String message) {
        return new ApiError(status, error, message, List.of(), Instant.now());
    }

    public static ApiError of(int status, String error, String message, List<String> details) {
        return new ApiError(status, error, message, details, Instant.now());
    }
}
