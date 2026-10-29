package wedding.com.example.weddingPackage.dto;

/**
 * A parsed Claude response plus the usage data we track for cost monitoring.
 */
public record AiResult<T>(T data, String model, long inputTokens, long outputTokens) {
}
