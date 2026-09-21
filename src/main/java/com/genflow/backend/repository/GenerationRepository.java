package com.genflow.backend.repository;

import com.genflow.backend.entity.Generation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenerationRepository
        extends JpaRepository<Generation, Long> {

    // Paginated generation history
    Page<Generation> findByUserId(
            Long userId,
            Pageable pageable
    );

    // Search user's generations by input
    Page<Generation> findByUserIdAndInputContainingIgnoreCase(
            Long userId,
            String query,
            Pageable pageable
    );
}