package wedding.com.example.weddingPackage.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import wedding.com.example.weddingPackage.model.WeddingPackage;

public interface WeddingPackageRepository extends MongoRepository<WeddingPackage, String> {

    Optional<WeddingPackage> findByCode(String code);

    /** Hard constraints: within budget and big enough for the guest list. Most expensive first. */
    List<WeddingPackage> findByPriceMyrLessThanEqualAndMaxGuestsGreaterThanEqualOrderByPriceMyrDesc(
            long budgetMyr, int guestCount);
}
