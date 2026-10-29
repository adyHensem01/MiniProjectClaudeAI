package wedding.com.example.weddingPackage.config;

import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;

@Configuration
@EnableConfigurationProperties(ClaudeProperties.class)
public class ClaudeConfig {

    /**
     * One shared, thread-safe Anthropic client for the whole application.
     * The API key is read from configuration (.env / environment) - never hard-coded.
     */
    @Bean
    public AnthropicClient anthropicClient(ClaudeProperties props) {
        if (props.apiKey() == null || props.apiKey().isBlank()) {
            throw new IllegalStateException(
                    "ANTHROPIC_API_KEY is not set. Copy .env.example to .env and add your key.");
        }
        return AnthropicOkHttpClient.builder()
                .apiKey(props.apiKey())
                .timeout(Duration.ofSeconds(props.timeoutSeconds()))
                .maxRetries(props.maxRetries())
                .build();
    }
}
