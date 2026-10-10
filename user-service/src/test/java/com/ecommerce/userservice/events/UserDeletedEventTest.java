package com.ecommerce.userservice.events;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserDeletedEventTest {

  @Test
  void getUserId_returnsIdPassedToConstructor() {
    UserDeletedEvent event = new UserDeletedEvent("user-1");

    assertThat(event.getUserId()).isEqualTo("user-1");
  }
}
