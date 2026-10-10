package com.ecommerce.userservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.ecommerce.userservice.dto.request.LoginRequest;
import com.ecommerce.userservice.dto.request.RegisterRequest;
import com.ecommerce.userservice.dto.response.ApiResponse;
import com.ecommerce.userservice.enums.UserRole;
import com.ecommerce.userservice.service.AuthService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock
  private AuthService authService;

  private AuthController authController;

  @BeforeEach
  void setUp() {
    authController = new AuthController(authService);
  }

  @Test
  void login_returns200WithToken() {
    LoginRequest request = new LoginRequest();
    request.setEmail("alice@example.com");
    request.setPassword("password123");
    ApiResponse<String> result = ApiResponse.success("Login successful", "jwt-token");
    when(authService.login(request)).thenReturn(result);

    var response = authController.login(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(result);
  }

  @Test
  void register_returns201() {
    RegisterRequest request = new RegisterRequest();
    request.setName("Alice");
    request.setEmail("alice@example.com");
    request.setPassword("password123");
    request.setRole(UserRole.CLIENT);
    ApiResponse<Void> result = ApiResponse.success("User registered successfully", null);
    when(authService.register(request)).thenReturn(result);

    var response = authController.register(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isEqualTo(result);
  }
}
