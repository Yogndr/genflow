package com.genflow.backend.repository;

import com.genflow.backend.entity.ImageGeneration;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageGenerationRepository
        extends JpaRepository<ImageGeneration, Long> {

    // Paginated image history
    Page<ImageGeneration> findByUserId(
            Long userId,
            Pageable pageable
    );

    // Search user's images by prompt
    Page<ImageGeneration> findByUserIdAndPromptContainingIgnoreCase(
            Long userId,
            String query,
            Pageable pageable
    );
}