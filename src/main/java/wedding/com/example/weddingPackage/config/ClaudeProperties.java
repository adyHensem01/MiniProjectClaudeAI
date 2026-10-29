package wedding.com.example.weddingPackage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Claude settings bound from application.properties (values come from .env / environment).
 */
@ConfigurationProperties(prefix = "claude")
public record ClaudeProperties(
        String apiKey,
        String model,
        long maxTokens,
        long timeoutSeconds,
        int maxRetries) {
}
