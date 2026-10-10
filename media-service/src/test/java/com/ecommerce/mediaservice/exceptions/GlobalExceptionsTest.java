package com.ecommerce.mediaservice.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.ecommerce.mediaservice.common.ResponseData;
import com.ecommerce.mediaservice.exceptions.Product.ForbiddenToChangeProductMediaException;
import com.ecommerce.mediaservice.exceptions.Product.MoreThanFiveImagesException;
import com.ecommerce.mediaservice.exceptions.Product.ProductNotFoundException;
import com.ecommerce.mediaservice.exceptions.Product.ProductServiceUnavailableException;
import com.ecommerce.mediaservice.exceptions.media.CloudinaryDeleteException;
import com.ecommerce.mediaservice.exceptions.media.CloudinaryUploadException;
import com.ecommerce.mediaservice.exceptions.media.ImageNotDeletedException;
import com.ecommerce.mediaservice.exceptions.media.ImageNotFoundException;
import com.ecommerce.mediaservice.exceptions.media.ImageNullOrEmptyException;
import com.ecommerce.mediaservice.exceptions.media.InvalidImageBodyException;
import com.ecommerce.mediaservice.exceptions.media.InvalidImageTypeException;
import com.ecommerce.mediaservice.exceptions.media.InvalidSizeLimitException;
import com.ecommerce.mediaservice.exceptions.media.MediaPersistenceException;
import com.ecommerce.mediaservice.exceptions.profile.ForbiddenToChangeProfileException;
import com.ecommerce.mediaservice.exceptions.profile.MoreThanOneImageException;

class GlobalExceptionsTest {

  private final GlobalExceptions handler = new GlobalExceptions();

  private void assertError(ResponseEntity<ResponseData<Void>> response, HttpStatus status, String message) {
    assertThat(response.getStatusCode()).isEqualTo(status);
    assertThat(response.getBody().isSuccess()).isFalse();
    assertThat(response.getBody().getMessage()).isEqualTo(message);
  }

  @Test
  void handleImageNotFoundException_returns404() {
    var response = handler.handleImageNotFoundException(new ImageNotFoundException("not found"));
    assertError(response, HttpStatus.NOT_FOUND, "not found");
  }

  @Test
  void handleInvalidImageBodyException_returns400() {
    var response = handler.handleInvalidImageBodyException(new InvalidImageBodyException("bad body"));
    assertError(response, HttpStatus.BAD_REQUEST, "bad body");
  }

  @Test
  void handleImageNullOrEmptyException_returns400() {
    var response = handler.handleImageNullOrEmptyException(new ImageNullOrEmptyException("empty"));
    assertError(response, HttpStatus.BAD_REQUEST, "empty");
  }

  @Test
  void handleInvalidImageTypeException_returns400() {
    var response = handler.handleInvalidImageTypeException(new InvalidImageTypeException("bad type"));
    assertError(response, HttpStatus.BAD_REQUEST, "bad type");
  }

  @Test
  void handleInvalidSizeLimitException_returns400() {
    var response = handler.handleInvalidSizeLimitException(new InvalidSizeLimitException("too big"));
    assertError(response, HttpStatus.BAD_REQUEST, "too big");
  }

  @Test
  void handleProductNotFoundException_returns404() {
    var response = handler.handleProductNotFoundException(new ProductNotFoundException("no product"));
    assertError(response, HttpStatus.NOT_FOUND, "no product");
  }

  @Test
  void handleProductServiceUnavailableException_returns502() {
    var response = handler.handleProductServiceUnavailableException(
        new ProductServiceUnavailableException("unavailable", new RuntimeException("down")));
    assertError(response, HttpStatus.BAD_GATEWAY, "unavailable");
  }

  @Test
  void handleCloudinaryUploadException_returns502() {
    var response = handler.handleCloudinaryUploadException(new CloudinaryUploadException("upload failed"));
    assertError(response, HttpStatus.BAD_GATEWAY, "upload failed");
  }

  @Test
  void handleCloudinaryDeleteException_returns502() {
    var response = handler.handleCloudinaryDeleteException(new CloudinaryDeleteException("delete failed"));
    assertError(response, HttpStatus.BAD_GATEWAY, "delete failed");
  }

  @Test
  void handleMediaPersistenceException_returns500() {
    var response = handler.handleMediaPersistenceException(new MediaPersistenceException("db failed"));
    assertError(response, HttpStatus.INTERNAL_SERVER_ERROR, "db failed");
  }

  @Test
  void handleForbiddenToChangeProfileException_returns403() {
    var response = handler.handleForbiddenToChangeProfileException(
        new ForbiddenToChangeProfileException("forbidden"));
    assertError(response, HttpStatus.FORBIDDEN, "forbidden");
  }

  @Test
  void handleForbiddenToChangeProductMediaException_returns403() {
    var response = handler.handleForbiddenToChangeProductMediaException(
        new ForbiddenToChangeProductMediaException("forbidden"));
    assertError(response, HttpStatus.FORBIDDEN, "forbidden");
  }

  @Test
  void handleImageNotDeletedException_returns500() {
    var response = handler.handleImageNotDeletedException(new ImageNotDeletedException("not deleted"));
    assertError(response, HttpStatus.INTERNAL_SERVER_ERROR, "not deleted");
  }

  @Test
  void handleMethodNotAllowedException_returns405() {
    HttpRequestMethodNotSupportedException ex = mock(HttpRequestMethodNotSupportedException.class);
    var response = handler.handleMethodNotAllowedException(ex);
    assertError(response, HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not supported for this endpoint");
  }

  @Test
  void handleMoreThanFiveImagesException_returns400() {
    var response = handler.handleMoreThanFiveImagesException(new MoreThanFiveImagesException("too many"));
    assertError(response, HttpStatus.BAD_REQUEST, "too many");
  }

  @Test
  void handleMoreThanOneImageException_returns400() {
    var response = handler.handleMoreThanOneImageException(new MoreThanOneImageException("too many"));
    assertError(response, HttpStatus.BAD_REQUEST, "too many");
  }

  @Test
  void handleNoResourceFoundException_returns404() {
    NoResourceFoundException ex = mock(NoResourceFoundException.class);
    var response = handler.handleNoResourceFoundException(ex);
    assertError(response, HttpStatus.NOT_FOUND, "The requested endpoint does not exist");
  }

  @Test
  void handleMethodArgumentNotValidException_returns400WithFirstFieldError() {
    BindingResult bindingResult = mock(BindingResult.class);
    FieldError fieldError = new FieldError("media", "targetId", "Invalid target Id !");
    when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

    MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
    when(ex.getBindingResult()).thenReturn(bindingResult);

    var response = handler.handleMethodArgumentNotValidException(ex);
    assertError(response, HttpStatus.BAD_REQUEST, "Invalid target Id !");
  }

  @Test
  void handleMethodArgumentNotValidException_noFieldErrors_returnsDefaultMessage() {
    BindingResult bindingResult = mock(BindingResult.class);
    when(bindingResult.getFieldErrors()).thenReturn(List.of());

    MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
    when(ex.getBindingResult()).thenReturn(bindingResult);

    var response = handler.handleMethodArgumentNotValidException(ex);
    assertError(response, HttpStatus.BAD_REQUEST, "Invalid request !");
  }

  @Test
  void handleMissingServletRequestPartException_returns400() {
    MissingServletRequestPartException ex = mock(MissingServletRequestPartException.class);
    when(ex.getRequestPartName()).thenReturn("data");

    var response = handler.handleMissingServletRequestPartException(ex);
    assertError(response, HttpStatus.BAD_REQUEST, "Required part 'data' is missing !");
  }

  @Test
  void handleHttpMessageNotReadableException_returns400() {
    HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
    var response = handler.handleHttpMessageNotReadableException(ex);
    assertError(response, HttpStatus.BAD_REQUEST, "Malformed request body !");
  }

  @Test
  void handleMissingRequestHeaderException_returns400() {
    MissingRequestHeaderException ex = mock(MissingRequestHeaderException.class);
    when(ex.getHeaderName()).thenReturn("X-User-Id");

    var response = handler.handleMissingRequestHeaderException(ex);
    assertError(response, HttpStatus.BAD_REQUEST, "Required header 'X-User-Id' is missing !");
  }

  @Test
  void handleAccessDeniedException_returns403() {
    AccessDeniedException ex = mock(AccessDeniedException.class);
    var response = handler.handleAccessDeniedException(ex);
    assertError(response, HttpStatus.FORBIDDEN, "You do not have permission to perform this action !");
  }

  @Test
  void mediaPersistenceException_withCause_keepsCause() {
    RuntimeException cause = new RuntimeException("db down");
    var ex = new MediaPersistenceException("db failed", cause);
    assertThat(ex.getCause()).isEqualTo(cause);
  }

  @Test
  void cloudinaryUploadException_withCause_keepsCause() {
    RuntimeException cause = new RuntimeException("io error");
    var ex = new CloudinaryUploadException("upload failed", cause);
    assertThat(ex.getCause()).isEqualTo(cause);
  }

  @Test
  void cloudinaryDeleteException_withCause_keepsCause() {
    RuntimeException cause = new RuntimeException("io error");
    var ex = new CloudinaryDeleteException("delete failed", cause);
    assertThat(ex.getCause()).isEqualTo(cause);
  }
}
