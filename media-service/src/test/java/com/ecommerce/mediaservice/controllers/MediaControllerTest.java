package com.ecommerce.mediaservice.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.mediaservice.common.ResponseData;
import com.ecommerce.mediaservice.dtos.DeleteMediaRequest;
import com.ecommerce.mediaservice.dtos.MediaRequest;
import com.ecommerce.mediaservice.dtos.TargetType;
import com.ecommerce.mediaservice.services.MediaService;

@ExtendWith(MockitoExtension.class)
class MediaControllerTest {

  @Mock
  private MediaService mediaService;

  @Mock
  private MultipartFile image;

  private MediaController mediaController;

  private static final String PRODUCT_ID = "product-1";
  private static final String USER_ID = "user-1";

  @BeforeEach
  void setUp() {
    mediaController = new MediaController(mediaService);
  }

  @Test
  void saveMedia_returns201WithServiceResult() {
    MediaRequest request = new MediaRequest(TargetType.PRODUCT, PRODUCT_ID, null);
    MultipartFile[] images = new MultipartFile[] { image };
    ResponseData<List<String>> result = ResponseData.success("saved", List.of("img-1"));
    when(mediaService.saveMedia(request, images)).thenReturn(result);

    var response = mediaController.saveMedia(request, images);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isEqualTo(result);
  }

  @Test
  void getProductsMedias_returns200WithServiceResult() {
    ResponseData<Map<String, List<String>>> result =
        ResponseData.success("ok", Map.of(PRODUCT_ID, List.of("img-1")));
    when(mediaService.getProductsMedias()).thenReturn(result);

    var response = mediaController.getProductsMedias();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(result);
  }

  @Test
  void getMedias_returns200WithServiceResult() {
    ResponseData<List<String>> result = ResponseData.success("ok", List.of("img-1"));
    when(mediaService.getMedias(PRODUCT_ID)).thenReturn(result);

    var response = mediaController.getMedias(PRODUCT_ID);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(result);
  }

  @Test
  void deleteMedia_returns200WithServiceResult() {
    DeleteMediaRequest request = new DeleteMediaRequest(TargetType.PRODUCT, PRODUCT_ID, List.of("img-1"));
    ResponseData<String> result = ResponseData.success("deleted", null);
    when(mediaService.deleteMedias(USER_ID, request)).thenReturn(result);

    var response = mediaController.deleteMedia(USER_ID, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(result);
  }

  @Test
  void updateMedia_returns200WithServiceResult() {
    MediaRequest request = new MediaRequest(TargetType.PRODUCT, PRODUCT_ID, null);
    MultipartFile[] images = new MultipartFile[] { image };
    ResponseData<List<String>> result = ResponseData.success("updated", List.of("img-2"));
    when(mediaService.updateMedias(USER_ID, request, images)).thenReturn(result);

    var response = mediaController.updateMedia(USER_ID, request, images);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(result);
  }
}
