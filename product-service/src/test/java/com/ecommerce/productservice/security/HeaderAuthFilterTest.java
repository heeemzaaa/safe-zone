package com.ecommerce.productservice.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class HeaderAuthFilterTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  private final HeaderAuthFilter filter = new HeaderAuthFilter();

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void headersPresent_setsAuthenticationAndContinuesChain() throws Exception {
    when(request.getHeader("X-User-Id")).thenReturn("user-1");
    when(request.getHeader("X-User-Role")).thenReturn("SELLER");

    filter.doFilterInternal(request, response, filterChain);

    var auth = SecurityContextHolder.getContext().getAuthentication();
    assertThat(auth).isNotNull();
    assertThat(auth.getName()).isEqualTo("user-1");
    assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_SELLER");
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void headersMissing_doesNotSetAuthentication_stillContinuesChain() throws Exception {
    when(request.getHeader("X-User-Id")).thenReturn(null);
    when(request.getHeader("X-User-Role")).thenReturn(null);

    filter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
  }
}
