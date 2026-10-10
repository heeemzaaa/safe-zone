package com.ecommerce.userservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.ecommerce.userservice.dto.request.UpdateProfileRequest;
import com.ecommerce.userservice.dto.response.ProfileResponse;
import com.ecommerce.userservice.enums.UserRole;
import com.ecommerce.userservice.service.ProfileService;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

  @Mock
  private ProfileService profileService;

  private ProfileController profileController;

  private static final String USER_ID = "user-1";

  @BeforeEach
  void setUp() {
    profileController = new ProfileController(profileService);
  }

  @Test
  void getProfile_returns200WithProfile() {
    ProfileResponse profile = ProfileResponse.builder()
        .name("Alice")
        .email("alice@example.com")
        .role(UserRole.CLIENT)
        .avatar(null)
        .build();
    when(profileService.getProfile(USER_ID)).thenReturn(profile);

    var response = profileController.getProfile(USER_ID);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData()).isEqualTo(profile);
  }

  @Test
  void updateProfile_returns200WithUpdatedProfile() {
    UpdateProfileRequest request = new UpdateProfileRequest();
    request.setName("New Name");
    request.setEmail("new@example.com");
    ProfileResponse updated = ProfileResponse.builder()
        .name("New Name")
        .email("new@example.com")
        .role(UserRole.CLIENT)
        .build();
    when(profileService.updateProfile(USER_ID, request)).thenReturn(updated);

    var response = profileController.updateProfile(USER_ID, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData()).isEqualTo(updated);
  }
}
