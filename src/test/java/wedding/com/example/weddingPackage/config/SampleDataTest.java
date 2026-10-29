package wedding.com.example.weddingPackage.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import wedding.com.example.weddingPackage.model.WeddingPackage;
import wedding.com.example.weddingPackage.service.AiOutputValidator;

class SampleDataTest {

    @Test
    void samplePackagesAreValid() throws Exception {
        List<WeddingPackage> packages;
        try (InputStream in = new ClassPathResource("sample_data/wedding_packages.json").getInputStream()) {
            packages = JsonMapper.builder().build().readValue(in, new TypeReference<List<WeddingPackage>>() {
            });
        }

        assertThat(packages).isNotEmpty();
        assertThat(packages).extracting(WeddingPackage::code).doesNotHaveDuplicates();
        for (WeddingPackage p : packages) {
            assertThat(p.priceMyr()).as(p.code()).isPositive();
            assertThat(p.maxGuests()).as(p.code()).isPositive();
            assertThat(AiOutputValidator.STYLES).as(p.code()).containsAll(p.styles());
            assertThat(AiOutputValidator.VENUE_TYPES).as(p.code()).contains(p.venueType());
            assertThat(p.inclusions()).as(p.code()).isNotEmpty();
        }
    }
}
