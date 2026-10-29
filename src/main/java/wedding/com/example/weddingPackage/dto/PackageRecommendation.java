package wedding.com.example.weddingPackage.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * Structured output Claude must return when choosing between approved packages.
 * The application checks that recommendedPackageCode is one of the candidates it supplied.
 */
public record PackageRecommendation(
        @JsonPropertyDescription("The code of the chosen package. Must be exactly one of the codes in <approved_packages>")
        String recommendedPackageCode,

        @JsonPropertyDescription("2 to 4 short reasons why this package suits the couple")
        List<String> reasons,

        @JsonPropertyDescription("A warm proposal message to the couple (max 1200 characters). Only mention facts from <approved_packages>")
        String proposalMessage) {
}
