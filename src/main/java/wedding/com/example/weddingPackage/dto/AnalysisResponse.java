package wedding.com.example.weddingPackage.dto;

/**
 * Response of POST /api/wedding/analyse.
 */
public record AnalysisResponse(WeddingAnalysis analysis, String model, long inputTokens, long outputTokens) {
}
