package com.ecommerce.mediaservice.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Api;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.api.ApiResponse;
import com.ecommerce.mediaservice.clients.ProductServiceGateway;
import com.ecommerce.mediaservice.dtos.Product;
import com.ecommerce.mediaservice.dtos.TargetType;
import com.ecommerce.mediaservice.exceptions.Product.ForbiddenToChangeProductMediaException;
import com.ecommerce.mediaservice.exceptions.Product.ProductNotFoundException;
import com.ecommerce.mediaservice.exceptions.media.CloudinaryDeleteException;
import com.ecommerce.mediaservice.exceptions.media.CloudinaryUploadException;
import com.ecommerce.mediaservice.exceptions.media.ImageNotFoundException;
import com.ecommerce.mediaservice.exceptions.media.ImageNullOrEmptyException;
import com.ecommerce.mediaservice.exceptions.media.InvalidImageBodyException;
import com.ecommerce.mediaservice.exceptions.media.InvalidImageTypeException;
import com.ecommerce.mediaservice.exceptions.media.InvalidSizeLimitException;
import com.ecommerce.mediaservice.exceptions.profile.ForbiddenToChangeProfileException;

@ExtendWith(MockitoExtension.class)
class MediaHelperTest {

  @Mock
  private Cloudinary cloudinary;

  @Mock
  private Uploader uploader;

  @Mock
  private Api api;

  @Mock
  private ProductServiceGateway productServiceGateway;

  @Mock
  private MultipartFile image;

  private MediaHelper mediaHelper;

  private static final String PRODUCT_ID = "product-1";
  private static final String USER_ID = "user-1";

  @BeforeEach
  void setUp() {
    mediaHelper = new MediaHelper(cloudinary, productServiceGateway);
  }

  private byte[] realPngBytes() throws IOException {
    BufferedImage bufferedImage = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ImageIO.write(bufferedImage, "png", out);
    return out.toByteArray();
  }

  // --- uploadToCloudinary ---

  @Test
  void uploadToCloudinary_product_returnsSecureUrl() throws IOException {
    when(cloudinary.uploader()).thenReturn(uploader);
    Map<String, Object> result = new HashMap<>();
    result.put("secure_url", "http://cloud/products/product-1/img.png");
    when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(result);
    when(image.getBytes()).thenReturn(new byte[] { 1, 2, 3 });

    String url = mediaHelper.uploadToCloudinary(image, TargetType.PRODUCT, PRODUCT_ID);

    assertThat(url).isEqualTo("http://cloud/products/product-1/img.png");
  }

  @Test
  void uploadToCloudinary_profile_returnsSecureUrl() throws IOException {
    when(cloudinary.uploader()).thenReturn(uploader);
    Map<String, Object> result = new HashMap<>();
    result.put("secure_url", "http://cloud/profile/user-1/img.png");
    when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(result);
    when(image.getBytes()).thenReturn(new byte[] { 1, 2, 3 });

    String url = mediaHelper.uploadToCloudinary(image, TargetType.PROFILE, USER_ID);

    assertThat(url).isEqualTo("http://cloud/profile/user-1/img.png");
  }

  @Test
  void uploadToCloudinary_ioExceptionReadingBytes_throwsCloudinaryUploadException() throws IOException {
    when(image.getBytes()).thenThrow(new IOException("read failed"));

    assertThatThrownBy(() -> mediaHelper.uploadToCloudinary(image, TargetType.PRODUCT, PRODUCT_ID))
        .isInstanceOf(CloudinaryUploadException.class);
  }

  @Test
  void uploadToCloudinary_noSecureUrlInResult_throwsCloudinaryUploadException() throws IOException {
    when(cloudinary.uploader()).thenReturn(uploader);
    when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(new HashMap<>());
    when(image.getBytes()).thenReturn(new byte[] { 1, 2, 3 });

    assertThatThrownBy(() -> mediaHelper.uploadToCloudinary(image, TargetType.PRODUCT, PRODUCT_ID))
        .isInstanceOf(CloudinaryUploadException.class);
  }

  // --- verifyImageBelongsToTarget ---

  @Test
  void verifyImageBelongsToTarget_product_pathMatches_doesNotThrow() {
    mediaHelper.verifyImageBelongsToTarget("http://cloud/products/product-1/img.png", TargetType.PRODUCT, PRODUCT_ID);
  }

  @Test
  void verifyImageBelongsToTarget_profile_pathMatches_doesNotThrow() {
    mediaHelper.verifyImageBelongsToTarget("http://cloud/profile/user-1/img.png", TargetType.PROFILE, USER_ID);
  }

  @Test
  void verifyImageBelongsToTarget_pathDoesNotMatch_throwsImageNotFoundException() {
    assertThatThrownBy(() -> mediaHelper.verifyImageBelongsToTarget(
        "http://cloud/products/other-product/img.png", TargetType.PRODUCT, PRODUCT_ID))
        .isInstanceOf(ImageNotFoundException.class);
  }

  // --- getFolder ---

  @Test
  void getFolder_product_returnsProductsFolder() {
    assertThat(mediaHelper.getFolder(TargetType.PRODUCT, PRODUCT_ID)).isEqualTo("products/product-1/");
  }

  @Test
  void getFolder_profile_returnsProfileFolder() {
    assertThat(mediaHelper.getFolder(TargetType.PROFILE, USER_ID)).isEqualTo("profile/user-1/");
  }

  // --- checkOwnership ---

  @Test
  void checkOwnership_profile_ownerMatches_doesNotThrow() {
    mediaHelper.checkOwnership(TargetType.PROFILE, USER_ID, USER_ID);
  }

  @Test
  void checkOwnership_profile_ownerDoesNotMatch_throwsForbidden() {
    assertThatThrownBy(() -> mediaHelper.checkOwnership(TargetType.PROFILE, USER_ID, "someone-else"))
        .isInstanceOf(ForbiddenToChangeProfileException.class);
  }

  @Test
  void checkOwnership_product_productNotFound_throwsProductNotFoundException() {
    when(productServiceGateway.getProduct(PRODUCT_ID)).thenReturn(null);

    assertThatThrownBy(() -> mediaHelper.checkOwnership(TargetType.PRODUCT, PRODUCT_ID, USER_ID))
        .isInstanceOf(ProductNotFoundException.class);
  }

  @Test
  void checkOwnership_product_ownerDoesNotMatch_throwsForbidden() {
    Product product = Product.builder().id(PRODUCT_ID).userId("someone-else").build();
    when(productServiceGateway.getProduct(PRODUCT_ID)).thenReturn(product);

    assertThatThrownBy(() -> mediaHelper.checkOwnership(TargetType.PRODUCT, PRODUCT_ID, USER_ID))
        .isInstanceOf(ForbiddenToChangeProductMediaException.class);
  }

  @Test
  void checkOwnership_product_ownerMatches_doesNotThrow() {
    Product product = Product.builder().id(PRODUCT_ID).userId(USER_ID).build();
    when(productServiceGateway.getProduct(PRODUCT_ID)).thenReturn(product);

    mediaHelper.checkOwnership(TargetType.PRODUCT, PRODUCT_ID, USER_ID);
  }

  // --- deleteFolderIfEmpty ---

  @Test
  void deleteFolderIfEmpty_noResources_deletesFolder() throws Exception {
    when(cloudinary.api()).thenReturn(api);
    ApiResponse resourcesResponse = mock(ApiResponse.class);
    when(resourcesResponse.get("resources")).thenReturn(List.of());
    when(api.resources(any(Map.class))).thenReturn(resourcesResponse);

    mediaHelper.deleteFolderIfEmpty("products/product-1/");

    verify(api).deleteFolder(eq("products/product-1/"), any(Map.class));
  }

  @Test
  void deleteFolderIfEmpty_hasResources_doesNotDeleteFolder() throws Exception {
    when(cloudinary.api()).thenReturn(api);
    ApiResponse resourcesResponse = mock(ApiResponse.class);
    when(resourcesResponse.get("resources")).thenReturn(List.of(Map.of("public_id", "x")));
    when(api.resources(any(Map.class))).thenReturn(resourcesResponse);

    mediaHelper.deleteFolderIfEmpty("products/product-1/");

    verify(api, never()).deleteFolder(any(), any());
  }

  @Test
  void deleteFolderIfEmpty_apiThrows_throwsCloudinaryDeleteException() throws Exception {
    when(cloudinary.api()).thenReturn(api);
    when(api.resources(any(Map.class))).thenThrow(new RuntimeException("cloudinary down"));

    assertThatThrownBy(() -> mediaHelper.deleteFolderIfEmpty("products/product-1/"))
        .isInstanceOf(CloudinaryDeleteException.class);
  }

  // --- deleteFromCloudinary ---

  @Test
  void deleteFromCloudinary_success_doesNotThrow() throws IOException {
    when(cloudinary.uploader()).thenReturn(uploader);
    Map<String, Object> result = Map.of("result", "ok");
    when(uploader.destroy(anyString(), any(Map.class))).thenReturn(result);

    mediaHelper.deleteFromCloudinary("http://cloud/upload/v123/products/product-1/img.png");
  }

  @Test
  void deleteFromCloudinary_withoutVersionPrefix_extractsPublicIdCorrectly() throws IOException {
    when(cloudinary.uploader()).thenReturn(uploader);
    Map<String, Object> result = Map.of("result", "ok");
    when(uploader.destroy(eq("products/product-1/img"), any(Map.class))).thenReturn(result);

    mediaHelper.deleteFromCloudinary("http://cloud/upload/products/product-1/img.png");
  }

  @Test
  void deleteFromCloudinary_statusNotOk_throwsImageNotFoundException() throws IOException {
    when(cloudinary.uploader()).thenReturn(uploader);
    Map<String, Object> result = Map.of("result", "not found");
    when(uploader.destroy(anyString(), any(Map.class))).thenReturn(result);

    assertThatThrownBy(() -> mediaHelper.deleteFromCloudinary("http://cloud/upload/v1/products/product-1/img.png"))
        .isInstanceOf(ImageNotFoundException.class);
  }

  @Test
  void deleteFromCloudinary_statusMissing_throwsImageNotFoundException() throws IOException {
    when(cloudinary.uploader()).thenReturn(uploader);
    when(uploader.destroy(anyString(), any(Map.class))).thenReturn(new HashMap<>());

    assertThatThrownBy(() -> mediaHelper.deleteFromCloudinary("http://cloud/upload/v1/products/product-1/img.png"))
        .isInstanceOf(ImageNotFoundException.class);
  }

  @Test
  void deleteFromCloudinary_ioException_throwsCloudinaryDeleteException() throws IOException {
    when(cloudinary.uploader()).thenReturn(uploader);
    when(uploader.destroy(anyString(), any(Map.class))).thenThrow(new IOException("down"));

    assertThatThrownBy(() -> mediaHelper.deleteFromCloudinary("http://cloud/upload/v1/products/product-1/img.png"))
        .isInstanceOf(CloudinaryDeleteException.class);
  }

  @Test
  void deleteFromCloudinary_invalidUrl_throwsCloudinaryDeleteException() {
    assertThatThrownBy(() -> mediaHelper.deleteFromCloudinary("http://cloud/no-upload-here/img.png"))
        .isInstanceOf(CloudinaryDeleteException.class);
  }

  // --- validateImage ---

  @Test
  void validateImage_nullImage_throwsImageNullOrEmptyException() {
    assertThatThrownBy(() -> mediaHelper.validateImage(null))
        .isInstanceOf(ImageNullOrEmptyException.class);
  }

  @Test
  void validateImage_emptyImage_throwsImageNullOrEmptyException() {
    when(image.isEmpty()).thenReturn(true);

    assertThatThrownBy(() -> mediaHelper.validateImage(image))
        .isInstanceOf(ImageNullOrEmptyException.class);
  }

  @Test
  void validateImage_tooBig_throwsInvalidSizeLimitException() {
    when(image.isEmpty()).thenReturn(false);
    when(image.getSize()).thenReturn(3L * 1024 * 1024);

    assertThatThrownBy(() -> mediaHelper.validateImage(image))
        .isInstanceOf(InvalidSizeLimitException.class);
  }

  @Test
  void validateImage_nullContentType_throwsInvalidImageTypeException() {
    when(image.isEmpty()).thenReturn(false);
    when(image.getSize()).thenReturn(100L);
    when(image.getContentType()).thenReturn(null);

    assertThatThrownBy(() -> mediaHelper.validateImage(image))
        .isInstanceOf(InvalidImageTypeException.class);
  }

  @Test
  void validateImage_nonImageContentType_throwsInvalidImageTypeException() {
    when(image.isEmpty()).thenReturn(false);
    when(image.getSize()).thenReturn(100L);
    when(image.getContentType()).thenReturn("application/pdf");

    assertThatThrownBy(() -> mediaHelper.validateImage(image))
        .isInstanceOf(InvalidImageTypeException.class);
  }

  @Test
  void validateImage_bodyIsNotAnImage_throwsInvalidImageBodyException() throws IOException {
    when(image.isEmpty()).thenReturn(false);
    when(image.getSize()).thenReturn(100L);
    when(image.getContentType()).thenReturn("image/png");
    when(image.getInputStream()).thenReturn(new ByteArrayInputStream("not an image".getBytes()));

    assertThatThrownBy(() -> mediaHelper.validateImage(image))
        .isInstanceOf(InvalidImageBodyException.class);
  }

  @Test
  void validateImage_ioExceptionReadingBody_throwsInvalidImageBodyException() throws IOException {
    when(image.isEmpty()).thenReturn(false);
    when(image.getSize()).thenReturn(100L);
    when(image.getContentType()).thenReturn("image/png");
    when(image.getInputStream()).thenThrow(new IOException("broken stream"));

    assertThatThrownBy(() -> mediaHelper.validateImage(image))
        .isInstanceOf(InvalidImageBodyException.class);
  }

  @Test
  void validateImage_validImage_doesNotThrow() throws IOException {
    when(image.isEmpty()).thenReturn(false);
    when(image.getSize()).thenReturn(100L);
    when(image.getContentType()).thenReturn("image/png");
    when(image.getInputStream()).thenReturn(new ByteArrayInputStream(realPngBytes()));

    mediaHelper.validateImage(image);
  }
}
