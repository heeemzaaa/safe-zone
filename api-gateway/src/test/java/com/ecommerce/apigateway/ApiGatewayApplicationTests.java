package com.ecommerce.apigateway;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApiGatewayApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void main_startsApplicationWithoutErrors() {
		assertDoesNotThrow(() -> ApiGatewayApplication.main(new String[] {
				"--server.port=0",
				"--server.ssl.enabled=false",
				"--server.ssl.key-store-password=test",
				"--eureka.client.register-with-eureka=false",
				"--eureka.client.fetch-registry=false"
		}));
	}

}
