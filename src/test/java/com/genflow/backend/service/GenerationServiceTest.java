package com.genflow.backend.service;

import com.genflow.backend.dto.GenerationRequest;
import com.genflow.backend.dto.GenerationResponse;
import com.genflow.backend.entity.Generation;
import com.genflow.backend.entity.GenerationType;
import com.genflow.backend.entity.User;
import com.genflow.backend.repository.GenerationRepository;
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
class GenerationServiceTest {

    @Mock
    private GenerationRepository generationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GeminiService geminiService;

    @InjectMocks
    private GenerationService generationService;

    private User user;

    @BeforeEach
    void setUp() {

        // Fake logged-in user
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "test@example.com",
                        null
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        // Fake user returned from repository
        user = new User();

        user.setId(1L);
        user.setEmail("test@example.com");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createGeneration_shouldGenerateAndSaveContent() {

        // Arrange
        GenerationRequest request =
                new GenerationRequest();

        request.setType(GenerationType.GENERATE);
        request.setInput("Explain REST APIs");

        when(
                userRepository.findByEmail(
                        "test@example.com"
                )
        ).thenReturn(Optional.of(user));

        when(
                geminiService.generateContent(anyString())
        ).thenReturn(
                "REST APIs allow applications to communicate over HTTP."
        );

        /*
         * GenerationService saves twice:
         *
         * 1. PENDING generation
         * 2. COMPLETED generation
         *
         * Return the same Generation object that the
         * service passes to the repository.
         */
        when(
                generationRepository.save(
                        any(Generation.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        // Act
        GenerationResponse response =
                generationService.createGeneration(request);

        // Assert
        assertNotNull(response);

        assertEquals(
                GenerationType.GENERATE,
                response.getType()
        );

        assertEquals(
                "Explain REST APIs",
                response.getInput()
        );

        assertEquals(
                "REST APIs allow applications to communicate over HTTP.",
                response.getOutput()
        );

        assertEquals(
                "COMPLETED",
                response.getStatus()
        );

        // Verify interactions
        verify(userRepository)
                .findByEmail("test@example.com");

        verify(geminiService)
                .generateContent(anyString());

        verify(
                generationRepository,
                times(2)
        ).save(any(Generation.class));
    }

    @Test
void createGeneration_shouldMarkAsFailedWhenGeminiThrowsException() {

    // Arrange
    GenerationRequest request =
            new GenerationRequest();

    request.setType(GenerationType.GENERATE);
    request.setInput("Explain microservices");

    when(
            userRepository.findByEmail(
                    "test@example.com"
            )
    ).thenReturn(Optional.of(user));

    when(
            geminiService.generateContent(anyString())
    ).thenThrow(
            new RuntimeException("Gemini API unavailable")
    );

    when(
            generationRepository.save(
                    any(Generation.class)
            )
    ).thenAnswer(invocation ->
            invocation.getArgument(0)
    );

    // Act
    GenerationResponse response =
            generationService.createGeneration(request);

    // Assert
    assertNotNull(response);

    assertEquals(
            "FAILED",
            response.getStatus()
    );

    assertNull(response.getOutput());

    // Verify
    verify(geminiService)
            .generateContent(anyString());

    verify(
            generationRepository,
            times(2)
    ).save(any(Generation.class));
}

@Test
void getGenerationById_shouldThrowForbiddenWhenGenerationBelongsToAnotherUser() {

    // Arrange
    User anotherUser = new User();
    anotherUser.setId(2L);
    anotherUser.setEmail("another@example.com");

    Generation generation = new Generation();
    generation.setId(10L);
    generation.setUser(anotherUser);

    when(
            userRepository.findByEmail("test@example.com")
    ).thenReturn(Optional.of(user));

    when(
            generationRepository.findById(10L)
    ).thenReturn(Optional.of(generation));

    // Act + Assert
    ForbiddenException exception =
            assertThrows(
                    ForbiddenException.class,
                    () ->
                            generationService
                                    .getGenerationById(10L)
            );

    assertEquals(
            "You are not authorized to access this generation",
            exception.getMessage()
    );

    verify(userRepository)
            .findByEmail("test@example.com");

    verify(generationRepository)
            .findById(10L);
}

@Test
void deleteGeneration_shouldThrowForbiddenWhenGenerationBelongsToAnotherUser() {

    // Arrange
    User anotherUser = new User();
    anotherUser.setId(2L);
    anotherUser.setEmail("another@example.com");

    Generation generation = new Generation();
    generation.setId(10L);
    generation.setUser(anotherUser);

    when(
            userRepository.findByEmail("test@example.com")
    ).thenReturn(Optional.of(user));

    when(
            generationRepository.findById(10L)
    ).thenReturn(Optional.of(generation));

    // Act + Assert
    assertThrows(
            ForbiddenException.class,
            () -> generationService.deleteGeneration(10L)
    );

    // Make sure unauthorized deletion NEVER happens
    verify(
            generationRepository,
            never()
    ).delete(any(Generation.class));
}
}