package wedding.com.example.weddingPackage.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * Structured output Claude must return when analysing a couple's request.
 * The JSON schema sent to Claude is generated from this record.
 * Valid JSON is not the same as valid application data, so AiOutputValidator
 * still checks every field before the application trusts it.
 */
public record WeddingAnalysis(
        @JsonPropertyDescription("One-sentence neutral summary of what the couple wants, max 300 characters")
        String summary,

        @JsonPropertyDescription("Main wedding style. One of: traditional, modern, garden, beach, rustic")
        String style,

        @JsonPropertyDescription("Preferred venue. One of: indoor, outdoor, either")
        String venueType,

        @JsonPropertyDescription("Up to 4 priorities, most important first. Allowed: catering, photography, decoration, venue, entertainment, attire")
        List<String> priorities,

        @JsonPropertyDescription("true if the couple asks for halal food or it is clearly implied")
        boolean halalRequired,

        @JsonPropertyDescription("true if the request is unclear, contradictory, unusual or needs a human planner")
        boolean needsHumanReview,

        @JsonPropertyDescription("Short reason when needsHumanReview is true, otherwise an empty string")
        String humanReviewReason) {
}
