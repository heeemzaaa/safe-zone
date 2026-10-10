package com.ecommerce.userservice.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApiResponseTest {

  @Test
  void success_setsSuccessTrueWithMessageAndData() {
    ApiResponse<String> response = ApiResponse.success("ok", "payload");

    assertThat(response.isSuccess()).isTrue();
    assertThat(response.getMessage()).isEqualTo("ok");
    assertThat(response.getData()).isEqualTo("payload");
  }

  @Test
  void error_setsSuccessFalseWithMessageAndNoData() {
    ApiResponse<String> response = ApiResponse.error("something went wrong");

    assertThat(response.isSuccess()).isFalse();
    assertThat(response.getMessage()).isEqualTo("something went wrong");
    assertThat(response.getData()).isNull();
  }
}
