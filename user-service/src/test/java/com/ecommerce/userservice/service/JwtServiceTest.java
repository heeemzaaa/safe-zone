package com.ecommerce.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ecommerce.userservice.enums.UserRole;
import com.ecommerce.userservice.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

class JwtServiceTest {

  private static final String SECRET = "test-secret-key-must-be-long-enough-for-hmac-256";
  private static final long EXPIRATION_MS = 3600000;

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService(SECRET, EXPIRATION_MS);
  }

  @Test
  void generateToken_containsUserIdAndRoleClaims() {
    User user = User.builder()
        .id("user-1")
        .name("Alice")
        .email("alice@example.com")
        .role(UserRole.CLIENT)
        .build();

    String token = jwtService.generateToken(user);

    SecretKey secretKey = Keys.hmacShaKeyFor(SECRET.getBytes());
    Claims claims = Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();

    assertThat(claims.getSubject()).isEqualTo("user-1");
    assertThat(claims.get("userId")).isEqualTo("user-1");
    assertThat(claims.get("role")).isEqualTo("CLIENT");
    assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
  }
}
