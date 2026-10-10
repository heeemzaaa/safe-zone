package com.ecommerce.discoveryserver;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DiscoveryServerApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void main_startsApplicationWithoutErrors() {
		assertDoesNotThrow(() -> DiscoveryServerApplication.main(new String[] {
				"--server.port=0",
				"--eureka.client.register-with-eureka=false",
				"--eureka.client.fetch-registry=false"
		}));
	}

}
