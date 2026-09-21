package com.genflow.backend.service;

import com.genflow.backend.dto.CloudinaryUploadResult;
import com.genflow.backend.dto.ImageGenerationRequest;
import com.genflow.backend.dto.ImageGenerationResponse;
import com.genflow.backend.entity.ImageGeneration;
import com.genflow.backend.entity.User;
import com.genflow.backend.exception.ForbiddenException;
import com.genflow.backend.exception.ResourceNotFoundException;
import com.genflow.backend.repository.ImageGenerationRepository;
import com.genflow.backend.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ImageGenerationService {

    private final ImageGenerationRepository imageGenerationRepository;
    private final UserRepository userRepository;
    private final HuggingFaceImageService huggingFaceImageService;
    private final CloudinaryService cloudinaryService;

    public ImageGenerationService(
            ImageGenerationRepository imageGenerationRepository,
            UserRepository userRepository,
            HuggingFaceImageService huggingFaceImageService,
            CloudinaryService cloudinaryService
    ) {
        this.imageGenerationRepository = imageGenerationRepository;
        this.userRepository = userRepository;
        this.huggingFaceImageService = huggingFaceImageService;
        this.cloudinaryService = cloudinaryService;
    }

    // Generate a new AI image
    public ImageGenerationResponse generateImage(
            ImageGenerationRequest request
    ) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        ImageGeneration imageGeneration = new ImageGeneration();

        imageGeneration.setUser(user);
        imageGeneration.setPrompt(request.getPrompt());
        imageGeneration.setStatus("PENDING");
        imageGeneration.setCreatedAt(LocalDateTime.now());
        imageGeneration.setUpdatedAt(LocalDateTime.now());

        ImageGeneration saved =
                imageGenerationRepository.save(imageGeneration);

        try {

            // 1. Generate image using Hugging Face
            byte[] imageBytes =
                    huggingFaceImageService.generateImage(
                            request.getPrompt()
                    );

            // 2. Upload generated image to Cloudinary
            CloudinaryUploadResult uploadResult =
                    cloudinaryService.uploadImage(imageBytes);

            // 3. Store Cloudinary information
            saved.setImageUrl(uploadResult.getImageUrl());
            saved.setPublicId(uploadResult.getPublicId());

            saved.setStatus("COMPLETED");
            saved.setUpdatedAt(LocalDateTime.now());

        } catch (Exception e) {

            System.err.println(
                    "Image generation failed: " + e.getMessage()
            );

            e.printStackTrace();

            saved.setStatus("FAILED");
            saved.setUpdatedAt(LocalDateTime.now());
        }

        saved = imageGenerationRepository.save(saved);

        return toResponse(saved);
    }

    // Get paginated image history
    public Page<ImageGenerationResponse> getMyImages(
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

        return imageGenerationRepository
                .findByUserId(user.getId(), pageable)
                .map(this::toResponse);
    }

    // Search logged-in user's images by prompt
    public Page<ImageGenerationResponse> searchMyImages(
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

        return imageGenerationRepository
                .findByUserIdAndPromptContainingIgnoreCase(
                        user.getId(),
                        query,
                        pageable
                )
                .map(this::toResponse);
    }

    // Get one image belonging to logged-in user
    public ImageGenerationResponse getImageById(Long id) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        ImageGeneration image =
                imageGenerationRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Image not found"
                                )
                        );

        if (!image.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException(
                    "You are not authorized to access this image"
            );
        }

        return toResponse(image);
    }

    // Delete image from Cloudinary and PostgreSQL
    public void deleteImage(Long id) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        ImageGeneration image =
                imageGenerationRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Image not found"
                                )
                        );

        if (!image.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException(
                    "You are not authorized to delete this image"
            );
        }

        // Delete actual image from Cloudinary
        if (image.getPublicId() != null &&
                !image.getPublicId().isBlank()) {

            cloudinaryService.deleteImage(
                    image.getPublicId()
            );
        }

        // Delete PostgreSQL record
        imageGenerationRepository.delete(image);
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
    private ImageGenerationResponse toResponse(
            ImageGeneration image
    ) {

        return new ImageGenerationResponse(
                image.getId(),
                image.getPrompt(),
                image.getImageUrl(),
                image.getStatus(),
                image.getCreatedAt(),
                image.getUpdatedAt()
        );
    }
}