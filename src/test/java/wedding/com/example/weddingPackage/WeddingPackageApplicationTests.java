package wedding.com.example.weddingPackage;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// A dummy key lets the context start without a real key; no Claude call is made.
@SpringBootTest(properties = "claude.api-key=test-key-not-real")
class WeddingPackageApplicationTests {

	@Test
	void contextLoads() {
	}

}
