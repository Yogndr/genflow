package com.genflow.backend.service;

import com.genflow.backend.dto.GenerationRequest;
import com.genflow.backend.dto.GenerationResponse;
import com.genflow.backend.entity.Generation;
import com.genflow.backend.entity.GenerationType;
import com.genflow.backend.entity.User;
import com.genflow.backend.exception.ForbiddenException;
import com.genflow.backend.exception.ResourceNotFoundException;
import com.genflow.backend.repository.GenerationRepository;
import com.genflow.backend.repository.UserRepository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GenerationService {

    private final GenerationRepository generationRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;

    public GenerationService(
            GenerationRepository generationRepository,
            UserRepository userRepository,
            GeminiService geminiService
    ) {
        this.generationRepository = generationRepository;
        this.userRepository = userRepository;
        this.geminiService = geminiService;
    }

    // Create new text generation
    public GenerationResponse createGeneration(
            GenerationRequest request
    ) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        Generation generation = new Generation();

        generation.setUser(user);
        generation.setType(request.getType());
        generation.setInput(request.getInput());
        generation.setStatus("PENDING");
        generation.setCreatedAt(LocalDateTime.now());
        generation.setUpdatedAt(LocalDateTime.now());

        Generation savedGeneration =
                generationRepository.save(generation);

        try {

            String prompt = buildPrompt(
                    request.getType(),
                    request.getInput()
            );

            String output =
                    geminiService.generateContent(prompt);

            savedGeneration.setOutput(output);
            savedGeneration.setStatus("COMPLETED");
            savedGeneration.setUpdatedAt(LocalDateTime.now());

        } catch (Exception e) {

            System.err.println(
                    "Gemini generation failed: " + e.getMessage()
            );

            e.printStackTrace();

            savedGeneration.setStatus("FAILED");
            savedGeneration.setUpdatedAt(LocalDateTime.now());
        }

        savedGeneration =
                generationRepository.save(savedGeneration);

        return toResponse(savedGeneration);
    }

    // Build Gemini prompt based on generation type
    private String buildPrompt(
            GenerationType type,
            String input
    ) {

        return switch (type) {

            case GENERATE ->
                    "Generate clear, well-structured content based on the following topic or instruction:\n\n"
                            + input;

            case REWRITE ->
                    "Rewrite the following content while preserving its original meaning. "
                            + "Improve clarity, grammar, and readability:\n\n"
                            + input;

            case EXPAND ->
                    "Expand the following content with useful details and explanation "
                            + "while staying relevant to the original idea:\n\n"
                            + input;

            case SHORTEN ->
                    "Shorten the following content while preserving the most important "
                            + "information and original meaning:\n\n"
                            + input;
        };
    }

    // Get paginated generation history
    public Page<GenerationResponse> getMyGenerations(
            int page,
            int size
    ) {

        validatePagination(page, size);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        return generationRepository
                .findByUserId(
                        user.getId(),
                        pageable
                )
                .map(this::toResponse);
    }

    // Search logged-in user's generations
    public Page<GenerationResponse> searchMyGenerations(
            String query,
            int page,
            int size
    ) {

        validatePagination(page, size);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        return generationRepository
                .findByUserIdAndInputContainingIgnoreCase(
                        user.getId(),
                        query,
                        pageable
                )
                .map(this::toResponse);
    }

    @Cacheable(
        value = "generations",
        key = "T(org.springframework.security.core.context.SecurityContextHolder)" +
              ".getContext().getAuthentication().getName() + ':' + #id"
)

    // Get one generation
    public GenerationResponse getGenerationById(Long id) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        Generation generation =
                generationRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Generation not found"
                                )
                        );

        if (!generation.getUser().getId().equals(user.getId())) {

            throw new ForbiddenException(
                    "You are not authorized to access this generation"
            );
        }

        return toResponse(generation);
    }

    @CacheEvict(
        value = "generations",
        key = "T(org.springframework.security.core.context.SecurityContextHolder)" +
              ".getContext().getAuthentication().getName() + ':' + #id"
)

    // Delete one generation
    public void deleteGeneration(Long id) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        Generation generation =
                generationRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Generation not found"
                                )
                        );

        if (!generation.getUser().getId().equals(user.getId())) {

            throw new ForbiddenException(
                    "You are not authorized to delete this generation"
            );
        }

        generationRepository.delete(generation);
    }

    // Validate pagination parameters
    private void validatePagination(
            int page,
            int size
    ) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative"
            );
        }

        if (size < 1 || size > 50) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 50"
            );
        }
    }

    // Convert entity to response DTO
    private GenerationResponse toResponse(
            Generation generation
    ) {

        return new GenerationResponse(
                generation.getId(),
                generation.getType(),
                generation.getInput(),
                generation.getOutput(),
                generation.getStatus(),
                generation.getCreatedAt(),
                generation.getUpdatedAt()
        );
    }
}