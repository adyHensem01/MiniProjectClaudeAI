package wedding.com.example.weddingPackage.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Component;

import wedding.com.example.weddingPackage.dto.PackageRecommendation;
import wedding.com.example.weddingPackage.dto.WeddingAnalysis;
import wedding.com.example.weddingPackage.exception.InvalidAiOutputException;

/**
 * Checks Claude's structured output BEFORE the application trusts it.
 * Valid JSON is not automatically valid application data.
 */
@Component
public class AiOutputValidator {

    public static final Set<String> STYLES = Set.of("traditional", "modern", "garden", "beach", "rustic");
    public static final Set<String> VENUE_TYPES = Set.of("indoor", "outdoor", "either");
    public static final Set<String> PRIORITIES =
            Set.of("catering", "photography", "decoration", "venue", "entertainment", "attire");

    /** Validates the analysis and returns a normalised copy (lower-case values). */
    public WeddingAnalysis validateAnalysis(WeddingAnalysis a) {
        if (a == null) {
            throw new InvalidAiOutputException("Claude returned an empty analysis", List.of("analysis is null"));
        }
        List<String> problems = new ArrayList<>();

        String summary = trim(a.summary());
        if (summary.isEmpty()) {
            problems.add("summary is missing");
        } else if (summary.length() > 300) {
            problems.add("summary is longer than 300 characters");
        }

        String style = lower(a.style());
        if (!STYLES.contains(style)) {
            problems.add("style '" + a.style() + "' is not one of " + STYLES);
        }

        String venueType = lower(a.venueType());
        if (!VENUE_TYPES.contains(venueType)) {
            problems.add("venueType '" + a.venueType() + "' is not one of " + VENUE_TYPES);
        }

        List<String> priorities = new ArrayList<>();
        if (a.priorities() == null) {
            problems.add("priorities is missing");
        } else {
            if (a.priorities().size() > 4) {
                problems.add("more than 4 priorities");
            }
            for (String p : a.priorities()) {
                String value = lower(p);
                if (!PRIORITIES.contains(value)) {
                    problems.add("priority '" + p + "' is not one of " + PRIORITIES);
                } else if (!priorities.contains(value)) {
                    priorities.add(value);
                }
            }
        }

        String reviewReason = trim(a.humanReviewReason());
        if (a.needsHumanReview() && reviewReason.isEmpty()) {
            problems.add("needsHumanReview is true but humanReviewReason is empty");
        }

        if (!problems.isEmpty()) {
            throw new InvalidAiOutputException("Claude's analysis failed validation", problems);
        }
        return new WeddingAnalysis(summary, style, venueType, List.copyOf(priorities),
                a.halalRequired(), a.needsHumanReview(), a.needsHumanReview() ? reviewReason : "");
    }

    /**
     * Validates a recommendation. The chosen code MUST be one the application supplied -
     * Claude is not allowed to invent or pick packages outside the approved list.
     */
    public PackageRecommendation validateRecommendation(PackageRecommendation r, Set<String> allowedCodes) {
        if (r == null) {
            throw new InvalidAiOutputException("Claude returned an empty recommendation", List.of("recommendation is null"));
        }
        List<String> problems = new ArrayList<>();

        String code = trim(r.recommendedPackageCode());
        if (!allowedCodes.contains(code)) {
            problems.add("recommendedPackageCode '" + code + "' is not one of the approved candidates " + allowedCodes);
        }

        List<String> reasons = r.reasons() == null ? List.of() : r.reasons().stream()
                .map(AiOutputValidator::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        if (reasons.isEmpty() || reasons.size() > 4) {
            problems.add("reasons must contain 1 to 4 items");
        }
        if (reasons.stream().anyMatch(s -> s.length() > 250)) {
            problems.add("a reason is longer than 250 characters");
        }

        String proposal = trim(r.proposalMessage());
        if (proposal.isEmpty()) {
            problems.add("proposalMessage is missing");
        } else if (proposal.length() > 1500) {
            problems.add("proposalMessage is longer than 1500 characters");
        }

        if (!problems.isEmpty()) {
            throw new InvalidAiOutputException("Claude's recommendation failed validation", problems);
        }
        return new PackageRecommendation(code, reasons, proposal);
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String lower(String s) {
        return trim(s).toLowerCase(Locale.ROOT);
    }
}
