package com.genflow.backend.service;

import com.genflow.backend.dto.ImageGenerationRequest;
import com.genflow.backend.dto.ImageGenerationResponse;
import com.genflow.backend.entity.ImageGeneration;
import com.genflow.backend.entity.User;
import com.genflow.backend.exception.ForbiddenException;
import com.genflow.backend.repository.ImageGenerationRepository;
import com.genflow.backend.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageGenerationServiceTest {

    @Mock
    private ImageGenerationRepository imageGenerationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HuggingFaceImageService huggingFaceImageService;

    @Mock
    private S3StorageService s3StorageService;

    @InjectMocks
    private ImageGenerationService imageGenerationService;

    private User user;

    @BeforeEach
    void setUp() {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "test@example.com",
                        null
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void generateImage_shouldGenerateUploadAndSaveImage() {

        // Arrange
        ImageGenerationRequest request =
                new ImageGenerationRequest();

        request.setPrompt(
                "A futuristic city at night"
        );

        byte[] fakeImageBytes =
                "fake-image".getBytes();

        String s3Key =
                "generated-images/test-image.png";

        String presignedUrl =
                "https://s3-presigned-url.example/test-image.png";

        when(
                userRepository.findByEmail(
                        "test@example.com"
                )
        ).thenReturn(Optional.of(user));

        when(
                huggingFaceImageService.generateImage(
                        "A futuristic city at night"
                )
        ).thenReturn(fakeImageBytes);

        when(
                s3StorageService.uploadImage(fakeImageBytes)
        ).thenReturn(s3Key);

        when(
                s3StorageService.generatePresignedUrl(s3Key)
        ).thenReturn(presignedUrl);

        when(
                imageGenerationRepository.save(
                        any(ImageGeneration.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        // Act
        ImageGenerationResponse response =
                imageGenerationService.generateImage(request);

        // Assert
        assertNotNull(response);

        assertEquals(
                "A futuristic city at night",
                response.getPrompt()
        );

        assertEquals(
                presignedUrl,
                response.getImageUrl()
        );

        assertEquals(
                "COMPLETED",
                response.getStatus()
        );

        // Verify pipeline
        verify(huggingFaceImageService)
                .generateImage(
                        "A futuristic city at night"
                );

        verify(s3StorageService)
                .uploadImage(fakeImageBytes);

        verify(s3StorageService)
                .generatePresignedUrl(s3Key);

        verify(
                imageGenerationRepository,
                times(2)
        ).save(any(ImageGeneration.class));
    }

    @Test
    void deleteImage_shouldDeleteFromS3AndDatabase() {

        // Arrange
        String s3Key =
                "generated-images/test-image.png";

        ImageGeneration image =
                new ImageGeneration();

        image.setId(10L);
        image.setUser(user);
        image.setPrompt("Test image");
        image.setImageUrl(s3Key);
image.setPublicId(s3Key);
image.setStatus("COMPLETED");

        when(
                userRepository.findByEmail(
                        "test@example.com"
                )
        ).thenReturn(Optional.of(user));

        when(
                imageGenerationRepository.findById(10L)
        ).thenReturn(Optional.of(image));

        // Act
        imageGenerationService.deleteImage(10L);

        // Assert / Verify
        verify(s3StorageService)
                .deleteFile(s3Key);

        verify(imageGenerationRepository)
                .delete(image);
    }

    @Test
    void deleteImage_shouldThrowForbiddenWhenImageBelongsToAnotherUser() {

        // Arrange
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setEmail("another@example.com");

        ImageGeneration image =
                new ImageGeneration();

        image.setId(10L);
        image.setUser(anotherUser);
        image.setPrompt("Another user's image");
        image.setImageUrl(
                "generated-images/another-image.png"
        );

        when(
                userRepository.findByEmail(
                        "test@example.com"
                )
        ).thenReturn(Optional.of(user));

        when(
                imageGenerationRepository.findById(10L)
        ).thenReturn(Optional.of(image));

        // Act + Assert
        ForbiddenException exception =
                assertThrows(
                        ForbiddenException.class,
                        () -> imageGenerationService.deleteImage(10L)
                );

        assertEquals(
                "You are not authorized to delete this image",
                exception.getMessage()
        );

        // Neither S3 nor PostgreSQL should be touched
        verify(
                s3StorageService,
                never()
        ).deleteFile(anyString());

        verify(
                imageGenerationRepository,
                never()
        ).delete(any(ImageGeneration.class));
    }
}