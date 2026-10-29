package wedding.com.example.weddingPackage.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * An approved wedding package from the vendor catalogue (trusted reference data).
 * Claude may only recommend packages that exist in this collection.
 */
@Document(collection = "wedding_packages")
public record WeddingPackage(
        @Id String id,
        @Indexed(unique = true) String code,
        String name,
        long priceMyr,
        int maxGuests,
        List<String> styles,
        String venueType,
        boolean halalCatering,
        List<String> inclusions) {
}
