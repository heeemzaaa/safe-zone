package com.ecommerce.productservice.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.ecommerce.productservice.dtos.request.ProductRequest;
import com.ecommerce.productservice.dtos.response.ProductPageResponse;
import com.ecommerce.productservice.dtos.response.ProductResponse;
import com.ecommerce.productservice.services.ProductService;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

  @Mock
  private ProductService productService;

  private ProductController productController;

  private static final String USER_ID = "user-1";
  private static final String PRODUCT_ID = "product-1";

  @BeforeEach
  void setUp() {
    productController = new ProductController(productService);
  }

  @Test
  void getAllProducts_returnsSuccessResponseWithServiceResult() {
    ProductPageResponse page = new ProductPageResponse(List.of(), 0, 20, 0, 0, false, false);
    when(productService.getProducts(null, 0, 20)).thenReturn(page);

    var response = productController.getAllProducts(null, 0, 20);

    assertThat(response.isSuccess()).isTrue();
    assertThat(response.getData()).isEqualTo(page);
  }

  @Test
  void getProduct_returnsSuccessResponseWithProduct() {
    ProductResponse product = new ProductResponse(
        PRODUCT_ID, "Chair", "Nice chair", BigDecimal.TEN, 5, USER_ID, List.of());
    when(productService.getProductById(PRODUCT_ID)).thenReturn(product);

    var response = productController.getProduct(PRODUCT_ID);

    assertThat(response.isSuccess()).isTrue();
    assertThat(response.getData()).isEqualTo(product);
  }

  @Test
  void createProduct_returns201WithCreatedProduct() {
    ProductRequest request = new ProductRequest("Chair", "Nice chair", BigDecimal.TEN, 5, List.of());
    ProductResponse created = new ProductResponse(
        PRODUCT_ID, "Chair", "Nice chair", BigDecimal.TEN, 5, USER_ID, List.of());
    when(productService.create(request, USER_ID)).thenReturn(created);

    var response = productController.createProduct(request, USER_ID);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().getData()).isEqualTo(created);
  }

  @Test
  void updateProduct_returns200WithUpdatedProduct() {
    ProductRequest request = new ProductRequest("New name", "New desc", BigDecimal.TEN, 5, List.of());
    ProductResponse updated = new ProductResponse(
        PRODUCT_ID, "New name", "New desc", BigDecimal.TEN, 5, USER_ID, List.of());
    when(productService.updateProduct(request, PRODUCT_ID, USER_ID)).thenReturn(updated);

    var response = productController.updateProduct(PRODUCT_ID, request, USER_ID);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData()).isEqualTo(updated);
  }

  @Test
  void deleteProduct_returns204AndCallsService() {
    var response = productController.deleteProduct(PRODUCT_ID, USER_ID);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(productService).deleteProduct(PRODUCT_ID, USER_ID);
  }
}
