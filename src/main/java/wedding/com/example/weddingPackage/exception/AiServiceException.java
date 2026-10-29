package wedding.com.example.weddingPackage.exception;

import org.springframework.http.HttpStatus;

/**
 * Claude could not be reached or rejected the request (network, rate limit, auth, 5xx).
 */
public class AiServiceException extends RuntimeException {

    private final HttpStatus status;

    public AiServiceException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
