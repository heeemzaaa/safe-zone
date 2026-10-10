package com.ecommerce.userservice.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.ecommerce.userservice.enums.UserRole;
import com.ecommerce.userservice.model.User;

class UserResponseTest {

  @Test
  void from_mapsFieldsFromUser() {
    User user = User.builder()
        .id("user-1")
        .name("Alice")
        .email("alice@example.com")
        .role(UserRole.CLIENT)
        .password("secret")
        .build();

    UserResponse response = UserResponse.from(user);

    assertThat(response.id()).isEqualTo("user-1");
    assertThat(response.name()).isEqualTo("Alice");
    assertThat(response.email()).isEqualTo("alice@example.com");
    assertThat(response.role()).isEqualTo(UserRole.CLIENT);
  }

  @Test
  void equalsAndHashCode_sameValues_areEqual() {
    UserResponse a = new UserResponse("user-1", "Alice", "alice@example.com", UserRole.CLIENT);
    UserResponse b = new UserResponse("user-1", "Alice", "alice@example.com", UserRole.CLIENT);

    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());
    assertThat(a.toString()).contains("Alice");
  }
}
