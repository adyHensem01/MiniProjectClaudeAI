package wedding.com.example.weddingPackage.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Incoming API request from a couple. Everything here is UNTRUSTED user input,
 * so it is validated before any Claude call or database query.
 */
public record WeddingRequest(
        @NotBlank(message = "coupleName is required")
        @Size(max = 100, message = "coupleName must be at most 100 characters")
        String coupleName,

        @NotBlank(message = "message is required")
        @Size(min = 10, max = 2000, message = "message must be between 10 and 2000 characters")
        String message,

        @NotNull(message = "budgetMyr is required")
        @Min(value = 5000, message = "budgetMyr must be at least 5000")
        @Max(value = 1000000, message = "budgetMyr must be at most 1000000")
        Long budgetMyr,

        @NotNull(message = "guestCount is required")
        @Min(value = 10, message = "guestCount must be at least 10")
        @Max(value = 2000, message = "guestCount must be at most 2000")
        Integer guestCount,

        @Future(message = "weddingDate must be in the future")
        LocalDate weddingDate) {
}
