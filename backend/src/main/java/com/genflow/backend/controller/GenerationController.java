package com.genflow.backend.controller;

import com.genflow.backend.dto.GenerationRequest;
import com.genflow.backend.dto.GenerationResponse;
import com.genflow.backend.service.GenerationService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/generations")
public class GenerationController {

    private final GenerationService generationService;

    public GenerationController(
            GenerationService generationService
    ) {
        this.generationService = generationService;
    }

    // Create a new text generation
    @PostMapping
    public ResponseEntity<GenerationResponse> createGeneration(
            @Valid @RequestBody GenerationRequest request
    ) {

        GenerationResponse response =
                generationService.createGeneration(request);

        return ResponseEntity.ok(response);
    }

    // Get paginated generation history
    @GetMapping
    public ResponseEntity<Page<GenerationResponse>> getMyGenerations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(
                generationService.getMyGenerations(page, size)
        );
    }

    @GetMapping("/search")
public ResponseEntity<Page<GenerationResponse>> searchGenerations(
        @RequestParam String query,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
) {

    return ResponseEntity.ok(
            generationService.searchMyGenerations(
                    query,
                    page,
                    size
            )
    );
}

    // Get one specific generation
    @GetMapping("/{id}")
    public ResponseEntity<GenerationResponse> getGenerationById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                generationService.getGenerationById(id)
        );
    }

    // Delete one generation
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGeneration(
            @PathVariable Long id
    ) {

        generationService.deleteGeneration(id);

        return ResponseEntity.noContent().build();
    }
}