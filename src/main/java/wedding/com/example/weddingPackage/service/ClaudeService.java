package wedding.com.example.weddingPackage.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.errors.AnthropicInvalidDataException;
import com.anthropic.errors.AnthropicIoException;
import com.anthropic.errors.AnthropicServiceException;
import com.anthropic.errors.BadRequestException;
import com.anthropic.errors.InternalServerException;
import com.anthropic.errors.PermissionDeniedException;
import com.anthropic.errors.RateLimitException;
import com.anthropic.errors.UnauthorizedException;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;

import wedding.com.example.weddingPackage.config.ClaudeProperties;
import wedding.com.example.weddingPackage.dto.AiResult;
import wedding.com.example.weddingPackage.exception.AiServiceException;
import wedding.com.example.weddingPackage.exception.InvalidAiOutputException;

/**
 * The ONLY class that talks to the Claude API.
 * Routes and workflow services never call the SDK directly.
 */
@Service
public class ClaudeService {

    private static final Logger log = LoggerFactory.getLogger(ClaudeService.class);

    private final AnthropicClient client;
    private final ClaudeProperties props;

    public ClaudeService(AnthropicClient client, ClaudeProperties props) {
        this.client = client;
        this.props = props;
    }

    /**
     * Sends a system prompt + user content and asks Claude to answer in the JSON
     * structure of {@code outputType} (structured outputs). The schema is generated
     * from the record, and the SDK parses the reply into that record.
     */
    public <T> AiResult<T> askStructured(String systemPrompt, String userContent, Class<T> outputType) {
        StructuredMessageCreateParams<T> params = MessageCreateParams.builder()
                .model(props.model())
                .maxTokens(props.maxTokens())
                .system(systemPrompt)
                .outputConfig(outputType)
                .addUserMessage(userContent)
                .build();

        try {
            StructuredMessage<T> response = client.messages().create(params);

            StopReason stopReason = response.stopReason().orElse(null);
            if (StopReason.REFUSAL.equals(stopReason)) {
                throw new InvalidAiOutputException("Claude declined to answer this request",
                        List.of("stop_reason=refusal"));
            }
            if (StopReason.MAX_TOKENS.equals(stopReason)) {
                throw new InvalidAiOutputException("Claude's answer was cut off before it was complete",
                        List.of("stop_reason=max_tokens"));
            }

            T data = response.content().stream()
                    .flatMap(block -> block.text().stream())
                    .map(typedText -> typedText.text())
                    .findFirst()
                    .orElseThrow(() -> new InvalidAiOutputException("Claude returned no structured content",
                            List.of("no text block in response")));

            long in = response.usage().inputTokens();
            long out = response.usage().outputTokens();
            log.info("Claude call ok: model={} type={} inputTokens={} outputTokens={}",
                    props.model(), outputType.getSimpleName(), in, out);
            return new AiResult<>(data, props.model(), in, out);

        // Most specific first: each category is handled differently.
        } catch (UnauthorizedException | PermissionDeniedException e) {
            throw new AiServiceException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "AI service is not configured correctly (check ANTHROPIC_API_KEY)", e);
        } catch (RateLimitException e) {
            throw new AiServiceException(HttpStatus.SERVICE_UNAVAILABLE,
                    "AI service is busy, please try again shortly", e);
        } catch (BadRequestException e) {
            throw new AiServiceException(HttpStatus.BAD_GATEWAY,
                    "AI service rejected the request", e);
        } catch (InternalServerException e) {
            throw new AiServiceException(HttpStatus.SERVICE_UNAVAILABLE,
                    "AI service is temporarily unavailable", e);
        } catch (AnthropicServiceException e) {
            throw new AiServiceException(HttpStatus.BAD_GATEWAY,
                    "AI service returned an error (HTTP " + e.statusCode() + ")", e);
        } catch (AnthropicIoException e) {
            throw new AiServiceException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Could not reach the AI service", e);
        } catch (AnthropicInvalidDataException e) {
            // Claude's reply could not be parsed into the expected record
            throw new InvalidAiOutputException("Claude returned data in an unexpected format",
                    List.of(e.getMessage()));
        }
    }
}
