package wedding.com.example.weddingPackage.model;

/**
 * Final status of a consultation. Decided by application code, never by Claude.
 */
public enum ConsultationStatus {
    /** A package from the catalogue was recommended. */
    RECOMMENDED,
    /** No approved package fits the budget / guest count / style. */
    NO_MATCH,
    /** The request needs a human wedding planner (unclear, unusual or risky). */
    NEEDS_REVIEW
}
