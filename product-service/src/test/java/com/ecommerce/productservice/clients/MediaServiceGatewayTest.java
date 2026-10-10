package com.ecommerce.productservice.clients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import feign.FeignException;
import feign.Request;

@ExtendWith(MockitoExtension.class)
class MediaServiceGatewayTest {

  @Mock
  private MediaServiceClient mediaServiceClient;

  private MediaServiceGateway mediaServiceGateway;

  private static final String PRODUCT_ID = "product-1";
  private static final String USER_ID = "user-1";

  @BeforeEach
  void setUp() {
    mediaServiceGateway = new MediaServiceGateway(mediaServiceClient);
  }

  @Test
  void getImages_returnsDataFromResponse() {
    MediaImagesResponse response = new MediaImagesResponse(true, "ok", List.of("img-1", "img-2"));
    when(mediaServiceClient.getMedias(PRODUCT_ID)).thenReturn(response);

    List<String> images = mediaServiceGateway.getImages(PRODUCT_ID);

    assertThat(images).containsExactly("img-1", "img-2");
  }

  @Test
  void getImages_responseDataIsNull_returnsEmptyList() {
    MediaImagesResponse response = new MediaImagesResponse(true, "ok", null);
    when(mediaServiceClient.getMedias(PRODUCT_ID)).thenReturn(response);

    List<String> images = mediaServiceGateway.getImages(PRODUCT_ID);

    assertThat(images).isEmpty();
  }

  @Test
  void getImages_productNotFound_returnsEmptyList() {
    Request request = Request.create(
        Request.HttpMethod.GET, "/media/images/" + PRODUCT_ID, Map.of(), null, null, null);
    when(mediaServiceClient.getMedias(PRODUCT_ID))
        .thenThrow(new FeignException.NotFound("not found", request, null, Map.of()));

    List<String> images = mediaServiceGateway.getImages(PRODUCT_ID);

    assertThat(images).isEmpty();
  }

  @Test
  void deleteProductMedia_noImages_doesNotCallDeleteMedia() {
    MediaImagesResponse response = new MediaImagesResponse(true, "ok", List.of());
    when(mediaServiceClient.getMedias(PRODUCT_ID)).thenReturn(response);

    mediaServiceGateway.deleteProductMedia(PRODUCT_ID, USER_ID);

    verify(mediaServiceClient, never()).deleteMedia(any(), any(), any());
  }

  @Test
  void deleteProductMedia_hasImages_callsDeleteMediaWithUserId() {
    MediaImagesResponse response = new MediaImagesResponse(true, "ok", List.of("img-1"));
    when(mediaServiceClient.getMedias(PRODUCT_ID)).thenReturn(response);

    mediaServiceGateway.deleteProductMedia(PRODUCT_ID, USER_ID);

    verify(mediaServiceClient).deleteMedia(
        USER_ID, "SELLER", new DeleteMediaRequest("PRODUCT", PRODUCT_ID, List.of("img-1")));
  }

  @Test
  void deleteProductMedia_deleteMediaFails_doesNotThrow() {
    MediaImagesResponse response = new MediaImagesResponse(true, "ok", List.of("img-1"));
    when(mediaServiceClient.getMedias(PRODUCT_ID)).thenReturn(response);
    doThrow(new RuntimeException("media-service down"))
        .when(mediaServiceClient).deleteMedia(any(), any(), any());

    mediaServiceGateway.deleteProductMedia(PRODUCT_ID, USER_ID);
    // no exception means the failure was caught, product deletion can go on
  }
}
