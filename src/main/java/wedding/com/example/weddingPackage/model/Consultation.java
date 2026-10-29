package wedding.com.example.weddingPackage.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import wedding.com.example.weddingPackage.dto.WeddingAnalysis;

/**
 * A saved consultation: the couple's request, Claude's validated analysis,
 * the packages the application allowed, the recommendation and token usage.
 */
@Document(collection = "consultations")
public record Consultation(
        @Id String id,
        String reference,
        String coupleName,
        String message,
        long budgetMyr,
        int guestCount,
        LocalDate weddingDate,
        WeddingAnalysis analysis,
        List<String> candidatePackageCodes,
        String recommendedPackageCode,
        List<String> reasons,
        String proposalMessage,
        ConsultationStatus status,
        String statusReason,
        String model,
        long inputTokens,
        long outputTokens,
        Instant createdAt) {
}
