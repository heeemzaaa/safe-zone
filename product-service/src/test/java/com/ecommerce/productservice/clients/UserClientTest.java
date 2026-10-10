package com.ecommerce.productservice.clients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.web.client.RestClient;

import com.ecommerce.productservice.dtos.response.UserResponse;

class UserClientTest {

  private static final String USER_ID = "user-1";

  @Test
  void getSeller_circuitBreakerRunsSupplier_returnsUserFromRestClient() {
    RestClient.Builder builder = mock(RestClient.Builder.class, Answers.RETURNS_DEEP_STUBS);
    CircuitBreakerFactory factory = mock(CircuitBreakerFactory.class);
    CircuitBreaker breaker = mock(CircuitBreaker.class);
    when(factory.create("userService")).thenReturn(breaker);

    UserResponse expected = new UserResponse(USER_ID, "Alice");
    when(builder.baseUrl("http://user-service").build()
        .get().uri("/users/{id}", USER_ID).retrieve().body(UserResponse.class))
        .thenReturn(expected);

    when(breaker.run(any(), any())).thenAnswer(invocation -> {
      Supplier<UserResponse> toRun = invocation.getArgument(0);
      return toRun.get();
    });

    UserClient userClient = new UserClient(builder, factory);
    UserResponse result = userClient.getSeller(USER_ID);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void getSeller_circuitBreakerRunsFallback_returnsFallbackUser() {
    RestClient.Builder builder = mock(RestClient.Builder.class, Answers.RETURNS_DEEP_STUBS);
    CircuitBreakerFactory factory = mock(CircuitBreakerFactory.class);
    CircuitBreaker breaker = mock(CircuitBreaker.class);
    when(factory.create("userService")).thenReturn(breaker);

    when(breaker.run(any(), any())).thenAnswer(invocation -> {
      Function<Throwable, UserResponse> fallback = invocation.getArgument(1);
      return fallback.apply(new RuntimeException("user-service down"));
    });

    UserClient userClient = new UserClient(builder, factory);
    UserResponse result = userClient.getSeller(USER_ID);

    assertThat(result.id()).isEqualTo(USER_ID);
    assertThat(result.name()).isEqualTo("Can't resolve this user");
  }
}
