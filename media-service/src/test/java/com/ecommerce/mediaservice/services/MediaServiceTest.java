package com.ecommerce.mediaservice.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.mediaservice.common.ResponseData;
import com.ecommerce.mediaservice.dtos.DeleteMediaRequest;
import com.ecommerce.mediaservice.dtos.MediaRequest;
import com.ecommerce.mediaservice.dtos.TargetType;
import com.ecommerce.mediaservice.exceptions.Product.MoreThanFiveImagesException;
import com.ecommerce.mediaservice.exceptions.media.ImageNotDeletedException;
import com.ecommerce.mediaservice.exceptions.media.ImageNotFoundException;
import com.ecommerce.mediaservice.exceptions.media.ImageNullOrEmptyException;
import com.ecommerce.mediaservice.exceptions.profile.MoreThanOneImageException;
import com.ecommerce.mediaservice.models.Media;
import com.ecommerce.mediaservice.repositories.MediaRepository;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private MediaHelper mediaHelper;

    @Mock
    private MultipartFile image;

    @InjectMocks
    private MediaService mediaService;

    private static final String PRODUCT_ID = "product-123";


    @Test
    void shouldRejectSaveWhenImagesAreNull() {

        MediaRequest request = new MediaRequest(TargetType.PRODUCT, PRODUCT_ID, null);

        assertThrows(
                ImageNullOrEmptyException.class,
                () -> mediaService.saveMedia(request, null)
        );
    }

    @Test
    void shouldRejectSaveWhenImagesAreEmpty() {

        MediaRequest request = new MediaRequest(TargetType.PRODUCT, PRODUCT_ID, null);

        assertThrows(
                ImageNullOrEmptyException.class,
                () -> mediaService.saveMedia(request, new MultipartFile[0])
        );
    }

    @Test
    void shouldRejectSaveWhenProductHasMoreThanFiveImages() {

        MediaRequest request = new MediaRequest(TargetType.PRODUCT, PRODUCT_ID, null);
        MultipartFile[] images = new MultipartFile[6];

        assertThrows(
                MoreThanFiveImagesException.class,
                () -> mediaService.saveMedia(request, images)
        );

        verify(mediaRepository, never()).save(any(Media.class));
    }

    @Test
    void shouldRejectSaveWhenProfileHasMoreThanOneImage() {

        MediaRequest request = new MediaRequest(TargetType.PROFILE, "user-123", null);
        MultipartFile[] images = new MultipartFile[2];

        assertThrows(
                MoreThanOneImageException.class,
                () -> mediaService.saveMedia(request, images)
        );

        verify(mediaRepository, never()).save(any(Media.class));
    }

    @Test
    void shouldSaveProductMediaSuccessfully() {

        MediaRequest request = new MediaRequest(TargetType.PRODUCT, PRODUCT_ID, null);
        MultipartFile[] images = new MultipartFile[] { image };

        when(mediaHelper.uploadToCloudinary(image, TargetType.PRODUCT, PRODUCT_ID))
                .thenReturn("http://cloud/image.png");

        ResponseData<List<String>> response = mediaService.saveMedia(request, images);

        assertTrue(response.isSuccess());
        assertEquals(List.of("http://cloud/image.png"), response.getData());

        verify(mediaHelper).validateImage(image);
        verify(mediaRepository).save(any(Media.class));
    }

    @Test
    void shouldNotPersistToRepositoryWhenSavingProfileMedia() {

        MediaRequest request = new MediaRequest(TargetType.PROFILE, "user-123", null);
        MultipartFile[] images = new MultipartFile[] { image };

        when(mediaHelper.uploadToCloudinary(image, TargetType.PROFILE, "user-123"))
                .thenReturn("http://cloud/avatar.png");

        ResponseData<List<String>> response = mediaService.saveMedia(request, images);

        assertEquals(List.of("http://cloud/avatar.png"), response.getData());

        verify(mediaRepository, never()).save(any(Media.class));
    }


    @Test
    void shouldGetMediasForProduct() {

        Media media1 = Media.builder().imagePath("img-1").productId(PRODUCT_ID).build();
        Media media2 = Media.builder().imagePath("img-2").productId(PRODUCT_ID).build();

        when(mediaRepository.findByProductId(PRODUCT_ID))
                .thenReturn(Optional.of(List.of(media1, media2)));

        ResponseData<List<String>> response = mediaService.getMedias(PRODUCT_ID);

        assertEquals(List.of("img-1", "img-2"), response.getData());
    }

    @Test
    void shouldReturnEmptyListWhenProductHasNoMedias() {

        when(mediaRepository.findByProductId(PRODUCT_ID))
                .thenReturn(Optional.empty());

        ResponseData<List<String>> response = mediaService.getMedias(PRODUCT_ID);

        assertTrue(response.getData().isEmpty());
    }

    @Test
    void shouldGroupMediasByProductId() {

        Media media1 = Media.builder().imagePath("img-1").productId("p1").build();
        Media media2 = Media.builder().imagePath("img-2").productId("p1").build();
        Media media3 = Media.builder().imagePath("img-3").productId("p2").build();

        when(mediaRepository.findAll()).thenReturn(List.of(media1, media2, media3));

        ResponseData<Map<String, List<String>>> response = mediaService.getProductsMedias();

        assertEquals(List.of("img-1", "img-2"), response.getData().get("p1"));
        assertEquals(List.of("img-3"), response.getData().get("p2"));
    }

    @Test
    void shouldSkipMediasWithoutProductId() {

        Media orphanMedia = Media.builder().imagePath("avatar.png").productId(null).build();

        when(mediaRepository.findAll()).thenReturn(List.of(orphanMedia));

        ResponseData<Map<String, List<String>>> response = mediaService.getProductsMedias();

        assertTrue(response.getData().isEmpty());
    }

    // --- deleteMedias ---

    @Test
    void deleteMedias_product_success_deletesMediaAndFolder() {
        DeleteMediaRequest request = new DeleteMediaRequest(TargetType.PRODUCT, PRODUCT_ID, List.of("img-1"));
        Media media = Media.builder().imagePath("img-1").productId(PRODUCT_ID).build();

        when(mediaRepository.findByImagePath("img-1")).thenReturn(Optional.of(media));
        when(mediaHelper.getFolder(TargetType.PRODUCT, PRODUCT_ID)).thenReturn("products/" + PRODUCT_ID + "/");

        ResponseData<String> response = mediaService.deleteMedias("user-123", request);

        assertTrue(response.isSuccess());
        verify(mediaHelper).checkOwnership(TargetType.PRODUCT, PRODUCT_ID, "user-123");
        verify(mediaHelper).deleteFromCloudinary("img-1");
        verify(mediaRepository).delete(media);
        verify(mediaHelper).deleteFolderIfEmpty("products/" + PRODUCT_ID + "/");
    }

    @Test
    void deleteMedias_product_imageNotInRepository_throwsImageNotFound() {
        DeleteMediaRequest request = new DeleteMediaRequest(TargetType.PRODUCT, PRODUCT_ID, List.of("img-1"));

        when(mediaRepository.findByImagePath("img-1")).thenReturn(Optional.empty());

        assertThrows(ImageNotFoundException.class, () -> mediaService.deleteMedias("user-123", request));
    }

    @Test
    void deleteMedias_product_imageBelongsToDifferentProduct_throwsImageNotFound() {
        DeleteMediaRequest request = new DeleteMediaRequest(TargetType.PRODUCT, PRODUCT_ID, List.of("img-1"));
        Media media = Media.builder().imagePath("img-1").productId("other-product").build();

        when(mediaRepository.findByImagePath("img-1")).thenReturn(Optional.of(media));

        assertThrows(ImageNotFoundException.class, () -> mediaService.deleteMedias("user-123", request));
    }

    @Test
    void deleteMedias_product_repositoryDeleteFails_throwsImageNotDeleted() {
        DeleteMediaRequest request = new DeleteMediaRequest(TargetType.PRODUCT, PRODUCT_ID, List.of("img-1"));
        Media media = Media.builder().imagePath("img-1").productId(PRODUCT_ID).build();

        when(mediaRepository.findByImagePath("img-1")).thenReturn(Optional.of(media));
        doThrow(new RuntimeException("db down")).when(mediaRepository).delete(media);

        assertThrows(ImageNotDeletedException.class, () -> mediaService.deleteMedias("user-123", request));
    }

    @Test
    void deleteMedias_profile_success_doesNotTouchRepository() {
        DeleteMediaRequest request = new DeleteMediaRequest(TargetType.PROFILE, "user-123", List.of("avatar.png"));

        when(mediaHelper.getFolder(TargetType.PROFILE, "user-123")).thenReturn("profile/user-123/");

        ResponseData<String> response = mediaService.deleteMedias("user-123", request);

        assertTrue(response.isSuccess());
        verify(mediaHelper).verifyImageBelongsToTarget("avatar.png", TargetType.PROFILE, "user-123");
        verify(mediaHelper).deleteFromCloudinary("avatar.png");
        verify(mediaRepository, never()).delete(any(Media.class));
    }

    // --- updateMedias ---

    @Test
    void updateMedias_imagesNullOrEmpty_throwsImageNullOrEmpty() {
        MediaRequest request = new MediaRequest(TargetType.PRODUCT, PRODUCT_ID, null);

        assertThrows(ImageNullOrEmptyException.class, () -> mediaService.updateMedias("user-123", request, null));
        assertThrows(ImageNullOrEmptyException.class,
                () -> mediaService.updateMedias("user-123", request, new MultipartFile[0]));
    }

    @Test
    void updateMedias_product_noOldImages_uploadsAndSavesNewMedia() {
        MediaRequest request = new MediaRequest(TargetType.PRODUCT, PRODUCT_ID, null);
        MultipartFile[] images = new MultipartFile[] { image };

        when(mediaHelper.uploadToCloudinary(image, TargetType.PRODUCT, PRODUCT_ID))
                .thenReturn("http://cloud/new-image.png");
        when(mediaHelper.getFolder(TargetType.PRODUCT, PRODUCT_ID)).thenReturn("products/" + PRODUCT_ID + "/");

        ResponseData<List<String>> response = mediaService.updateMedias("user-123", request, images);

        assertEquals(List.of("http://cloud/new-image.png"), response.getData());
        verify(mediaHelper).validateImage(image);
        verify(mediaRepository).save(any(Media.class));
        verify(mediaHelper).deleteFolderIfEmpty("products/" + PRODUCT_ID + "/");
    }

    @Test
    void updateMedias_product_withOldImages_deletesOldAndUploadsNew() {
        MediaRequest request = new MediaRequest(TargetType.PRODUCT, PRODUCT_ID, List.of("old-img"));
        MultipartFile[] images = new MultipartFile[] { image };
        Media oldMedia = Media.builder().imagePath("old-img").productId(PRODUCT_ID).build();

        when(mediaRepository.findByImagePath("old-img")).thenReturn(Optional.of(oldMedia));
        when(mediaHelper.uploadToCloudinary(image, TargetType.PRODUCT, PRODUCT_ID))
                .thenReturn("http://cloud/new-image.png");

        ResponseData<List<String>> response = mediaService.updateMedias("user-123", request, images);

        assertEquals(List.of("http://cloud/new-image.png"), response.getData());
        verify(mediaHelper).verifyImageBelongsToTarget("old-img", TargetType.PRODUCT, PRODUCT_ID);
        verify(mediaHelper).deleteFromCloudinary("old-img");
        verify(mediaRepository).delete(oldMedia);
    }

    @Test
    void updateMedias_oldImageNotInRepository_throwsImageNotFound() {
        MediaRequest request = new MediaRequest(TargetType.PRODUCT, PRODUCT_ID, List.of("old-img"));
        MultipartFile[] images = new MultipartFile[] { image };

        when(mediaRepository.findByImagePath("old-img")).thenReturn(Optional.empty());

        assertThrows(ImageNotFoundException.class,
                () -> mediaService.updateMedias("user-123", request, images));
    }

    @Test
    void updateMedias_oldImageRepositoryDeleteFails_throwsImageNotDeleted() {
        MediaRequest request = new MediaRequest(TargetType.PRODUCT, PRODUCT_ID, List.of("old-img"));
        MultipartFile[] images = new MultipartFile[] { image };
        Media oldMedia = Media.builder().imagePath("old-img").productId(PRODUCT_ID).build();

        when(mediaRepository.findByImagePath("old-img")).thenReturn(Optional.of(oldMedia));
        doThrow(new RuntimeException("db down")).when(mediaRepository).delete(oldMedia);

        assertThrows(ImageNotDeletedException.class,
                () -> mediaService.updateMedias("user-123", request, images));
    }

    @Test
    void updateMedias_profile_withOldImages_deletesOldWithoutRepositoryCalls() {
        MediaRequest request = new MediaRequest(TargetType.PROFILE, "user-123", List.of("old-avatar.png"));
        MultipartFile[] images = new MultipartFile[] { image };

        when(mediaHelper.uploadToCloudinary(image, TargetType.PROFILE, "user-123"))
                .thenReturn("http://cloud/new-avatar.png");

        ResponseData<List<String>> response = mediaService.updateMedias("user-123", request, images);

        assertEquals(List.of("http://cloud/new-avatar.png"), response.getData());
        verify(mediaHelper).deleteFromCloudinary("old-avatar.png");
        verify(mediaRepository, never()).delete(any(Media.class));
        verify(mediaRepository, never()).save(any(Media.class));
    }
}
