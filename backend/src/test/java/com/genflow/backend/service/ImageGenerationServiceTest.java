package com.genflow.backend.service;

import com.genflow.backend.dto.CloudinaryUploadResult;
import com.genflow.backend.dto.ImageGenerationRequest;
import com.genflow.backend.dto.ImageGenerationResponse;
import com.genflow.backend.entity.ImageGeneration;
import com.genflow.backend.entity.User;
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
import com.genflow.backend.exception.ForbiddenException;

@ExtendWith(MockitoExtension.class)
class ImageGenerationServiceTest {

    @Mock
    private ImageGenerationRepository imageGenerationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HuggingFaceImageService huggingFaceImageService;

    @Mock
    private CloudinaryService cloudinaryService;

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

        CloudinaryUploadResult uploadResult =
                new CloudinaryUploadResult(
                        "https://cloudinary.com/test-image.jpg",
                        "genflow/test-image"
                );

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
                cloudinaryService.uploadImage(fakeImageBytes)
        ).thenReturn(uploadResult);

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
                "https://cloudinary.com/test-image.jpg",
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

        verify(cloudinaryService)
                .uploadImage(fakeImageBytes);

        verify(
                imageGenerationRepository,
                times(2)
        ).save(any(ImageGeneration.class));
    }

    @Test
void deleteImage_shouldDeleteFromCloudinaryAndDatabase() {

    // Arrange
    ImageGeneration image = new ImageGeneration();

    image.setId(10L);
    image.setUser(user);
    image.setPrompt("Test image");
    image.setImageUrl(
            "https://cloudinary.com/test-image.jpg"
    );
    image.setPublicId("genflow/test-image");
    image.setStatus("COMPLETED");

    when(
            userRepository.findByEmail("test@example.com")
    ).thenReturn(Optional.of(user));

    when(
            imageGenerationRepository.findById(10L)
    ).thenReturn(Optional.of(image));

    // Act
    imageGenerationService.deleteImage(10L);

    // Assert / Verify
    verify(cloudinaryService)
            .deleteImage("genflow/test-image");

    verify(imageGenerationRepository)
            .delete(image);
}

@Test
void deleteImage_shouldThrowForbiddenWhenImageBelongsToAnotherUser() {

    // Arrange
    User anotherUser = new User();
    anotherUser.setId(2L);
    anotherUser.setEmail("another@example.com");

    ImageGeneration image = new ImageGeneration();

    image.setId(10L);
    image.setUser(anotherUser);
    image.setPrompt("Another user's image");
    image.setPublicId("genflow/another-image");

    when(
            userRepository.findByEmail("test@example.com")
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

    // Most important checks:
    // neither Cloudinary nor PostgreSQL should be touched.
    verify(
            cloudinaryService,
            never()
    ).deleteImage(anyString());

    verify(
            imageGenerationRepository,
            never()
    ).delete(any(ImageGeneration.class));
}
}