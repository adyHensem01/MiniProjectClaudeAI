package wedding.com.example.weddingPackage.exception;

import java.util.List;

/**
 * Claude answered, but the answer failed our validation and must not be trusted.
 */
public class InvalidAiOutputException extends RuntimeException {

    private final List<String> problems;

    public InvalidAiOutputException(String message, List<String> problems) {
        super(message);
        this.problems = List.copyOf(problems);
    }

    public List<String> getProblems() {
        return problems;
    }
}
