package com.ecommerce.mediaservice.clients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.mediaservice.common.ResponseData;
import com.ecommerce.mediaservice.dtos.Product;

import feign.FeignException;
import feign.Request;

@ExtendWith(MockitoExtension.class)
class ProductServiceGatewayTest {

  @Mock
  private ProductServiceClient productServiceClient;

  private ProductServiceGateway productServiceGateway;

  private static final String PRODUCT_ID = "product-1";

  @BeforeEach
  void setUp() {
    productServiceGateway = new ProductServiceGateway(productServiceClient);
  }

  @Test
  void getProduct_returnsProductFromResponse() {
    Product product = Product.builder()
        .id(PRODUCT_ID)
        .name("Chair")
        .description("Nice chair")
        .price(BigDecimal.TEN)
        .quantity(5)
        .userId("seller-1")
        .imageUrls(List.of())
        .build();
    when(productServiceClient.getProduct(PRODUCT_ID))
        .thenReturn(ResponseData.success("ok", product));

    Product result = productServiceGateway.getProduct(PRODUCT_ID);

    assertThat(result).isEqualTo(product);
  }

  @Test
  void getProduct_responseDataIsNull_returnsNull() {
    when(productServiceClient.getProduct(PRODUCT_ID))
        .thenReturn(ResponseData.success("ok", null));

    Product result = productServiceGateway.getProduct(PRODUCT_ID);

    assertThat(result).isNull();
  }

  @Test
  void getProduct_productNotFound_returnsNull() {
    Request request = Request.create(
        Request.HttpMethod.GET, "/products/" + PRODUCT_ID, Map.of(), null, null, null);
    when(productServiceClient.getProduct(PRODUCT_ID))
        .thenThrow(new FeignException.NotFound("not found", request, null, Map.of()));

    Product result = productServiceGateway.getProduct(PRODUCT_ID);

    assertThat(result).isNull();
  }
}
