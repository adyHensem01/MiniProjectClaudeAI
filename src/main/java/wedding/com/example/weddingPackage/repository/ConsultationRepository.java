package wedding.com.example.weddingPackage.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import wedding.com.example.weddingPackage.model.Consultation;

public interface ConsultationRepository extends MongoRepository<Consultation, String> {

    Optional<Consultation> findByReference(String reference);
}
