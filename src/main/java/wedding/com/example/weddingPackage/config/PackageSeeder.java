package wedding.com.example.weddingPackage.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import wedding.com.example.weddingPackage.model.WeddingPackage;
import wedding.com.example.weddingPackage.repository.ConsultationRepository;
import wedding.com.example.weddingPackage.repository.WeddingPackageRepository;

/**
 * Loads the approved package catalogue from sample_data/wedding_packages.json on startup.
 *
 * Normal mode: idempotent upsert by package code (safe to run every start).
 * Reset mode (SEED_RESET=true): clears packages and consultations first.
 */
@Component
public class PackageSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PackageSeeder.class);
    private static final String PACKAGES_FILE = "sample_data/wedding_packages.json";

    private final WeddingPackageRepository packageRepository;
    private final ConsultationRepository consultationRepository;
    private final JsonMapper jsonMapper;
    private final boolean reset;

    public PackageSeeder(WeddingPackageRepository packageRepository, ConsultationRepository consultationRepository,
            JsonMapper jsonMapper, @Value("${app.seed.reset:false}") boolean reset) {
        this.packageRepository = packageRepository;
        this.consultationRepository = consultationRepository;
        this.jsonMapper = jsonMapper;
        this.reset = reset;
    }

    @Override
    public void run(String... args) {
        try {
            if (reset) {
                consultationRepository.deleteAll();
                packageRepository.deleteAll();
            }

            for (WeddingPackage pkg : loadPackages()) {
                // Upsert by code: reuse the existing MongoDB id so the document is replaced, not duplicated
                String existingId = packageRepository.findByCode(pkg.code()).map(WeddingPackage::id).orElse(null);
                packageRepository.save(new WeddingPackage(existingId, pkg.code(), pkg.name(), pkg.priceMyr(),
                        pkg.maxGuests(), pkg.styles(), pkg.venueType(), pkg.halalCatering(), pkg.inclusions()));
            }

            log.info("Packages available: {} | Mode: {}", packageRepository.count(),
                    reset ? "reset and seed" : "idempotent upsert");
        } catch (IOException e) {
            log.error("Could not read {}: {}", PACKAGES_FILE, e.getMessage());
        } catch (Exception e) {
            // Keep the app running so the API can return a controlled 503 instead of failing to start
            log.warn("Could not seed packages - is MongoDB running? {}", e.getMessage());
        }
    }

    private List<WeddingPackage> loadPackages() throws IOException {
        try (InputStream in = new ClassPathResource(PACKAGES_FILE).getInputStream()) {
            return jsonMapper.readValue(in, new TypeReference<List<WeddingPackage>>() {
            });
        }
    }
}
