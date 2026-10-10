package com.ecommerce.apigateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.function.ServerRequest;

import com.ecommerce.apigateway.security.JwtAuthenticationFilter;

class UserClaimsGatewayFilterTest {

  private final UserClaimsGatewayFilter filter = new UserClaimsGatewayFilter();

  @Test
  void apply_attributesPresent_addsTrustedHeadersToRequest() {
    MockHttpServletRequest httpRequest = new MockHttpServletRequest();
    httpRequest.setAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE, "user-1");
    httpRequest.setAttribute(JwtAuthenticationFilter.USER_ROLE_ATTRIBUTE, "SELLER");
    ServerRequest request = ServerRequest.create(httpRequest, List.of());

    ServerRequest result = filter.apply(request);

    assertThat(result.headers().firstHeader("X-User-Id")).isEqualTo("user-1");
    assertThat(result.headers().firstHeader("X-User-Role")).isEqualTo("SELLER");
  }

  @Test
  void apply_attributesPresent_removesAnyClientSuppliedHeadersFirst() {
    MockHttpServletRequest httpRequest = new MockHttpServletRequest();
    httpRequest.addHeader("X-User-Id", "attacker-supplied");
    httpRequest.addHeader("X-User-Role", "ADMIN");
    httpRequest.setAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE, "user-1");
    httpRequest.setAttribute(JwtAuthenticationFilter.USER_ROLE_ATTRIBUTE, "SELLER");
    ServerRequest request = ServerRequest.create(httpRequest, List.of());

    ServerRequest result = filter.apply(request);

    assertThat(result.headers().header("X-User-Id")).containsExactly("user-1");
    assertThat(result.headers().header("X-User-Role")).containsExactly("SELLER");
  }

  @Test
  void apply_attributesMissing_returnsSameRequestUnchanged() {
    MockHttpServletRequest httpRequest = new MockHttpServletRequest();
    ServerRequest request = ServerRequest.create(httpRequest, List.of());

    ServerRequest result = filter.apply(request);

    assertThat(result).isSameAs(request);
  }

  @Test
  void apply_onlyUserIdPresent_returnsSameRequestUnchanged() {
    MockHttpServletRequest httpRequest = new MockHttpServletRequest();
    httpRequest.setAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE, "user-1");
    ServerRequest request = ServerRequest.create(httpRequest, List.of());

    ServerRequest result = filter.apply(request);

    assertThat(result).isSameAs(request);
  }
}
