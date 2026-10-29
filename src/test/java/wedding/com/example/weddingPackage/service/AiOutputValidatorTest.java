package wedding.com.example.weddingPackage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import wedding.com.example.weddingPackage.dto.PackageRecommendation;
import wedding.com.example.weddingPackage.dto.WeddingAnalysis;
import wedding.com.example.weddingPackage.exception.InvalidAiOutputException;

class AiOutputValidatorTest {

    private final AiOutputValidator validator = new AiOutputValidator();

    @Test
    void validAnalysisIsNormalised() {
        WeddingAnalysis raw = new WeddingAnalysis("  Couple wants a garden wedding. ", "Garden", "OUTDOOR",
                List.of("Catering", "photography", "catering"), true, false, "");

        WeddingAnalysis result = validator.validateAnalysis(raw);

        assertThat(result.style()).isEqualTo("garden");
        assertThat(result.venueType()).isEqualTo("outdoor");
        assertThat(result.priorities()).containsExactly("catering", "photography");
        assertThat(result.summary()).isEqualTo("Couple wants a garden wedding.");
    }

    @Test
    void validJsonButInvalidValuesIsRejected() {
        WeddingAnalysis raw = new WeddingAnalysis("Angry couple", "super_fancy", "space_station",
                List.of("fireworks"), false, true, "");

        assertThatThrownBy(() -> validator.validateAnalysis(raw))
                .isInstanceOf(InvalidAiOutputException.class)
                .satisfies(e -> assertThat(((InvalidAiOutputException) e).getProblems()).hasSize(4));
    }

    @Test
    void recommendationOutsideApprovedCandidatesIsRejected() {
        PackageRecommendation rec = new PackageRecommendation("FREE-VIP-999",
                List.of("It is free"), "Congratulations!");

        assertThatThrownBy(() -> validator.validateRecommendation(rec, Set.of("GRD-BLOOM-200", "MOD-CITY-250")))
                .isInstanceOf(InvalidAiOutputException.class)
                .hasMessageContaining("recommendation failed validation");
    }

    @Test
    void recommendationFromCandidatesIsAccepted() {
        PackageRecommendation rec = new PackageRecommendation(" GRD-BLOOM-200 ",
                List.of("Fits your garden theme", "Within budget"), "Dear Aina & Hafiz, ...");

        PackageRecommendation result = validator.validateRecommendation(rec, Set.of("GRD-BLOOM-200"));

        assertThat(result.recommendedPackageCode()).isEqualTo("GRD-BLOOM-200");
    }
}
