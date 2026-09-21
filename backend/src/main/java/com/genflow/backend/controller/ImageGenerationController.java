package com.genflow.backend.controller;

import com.genflow.backend.dto.ImageGenerationRequest;
import com.genflow.backend.dto.ImageGenerationResponse;
import com.genflow.backend.service.ImageGenerationService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/images")
public class ImageGenerationController {

    private final ImageGenerationService imageGenerationService;

    public ImageGenerationController(
            ImageGenerationService imageGenerationService
    ) {
        this.imageGenerationService = imageGenerationService;
    }

    // Generate a new AI image
    @PostMapping("/generate")
    public ResponseEntity<ImageGenerationResponse> generateImage(
            @Valid @RequestBody ImageGenerationRequest request
    ) {

        ImageGenerationResponse response =
                imageGenerationService.generateImage(request);

        return ResponseEntity.ok(response);
    }

    // Get paginated image generation history
    @GetMapping
    public ResponseEntity<Page<ImageGenerationResponse>> getMyImages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(
                imageGenerationService.getMyImages(page, size)
        );
    }

    @GetMapping("/search")
public ResponseEntity<Page<ImageGenerationResponse>> searchImages(
        @RequestParam String query,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
) {

    return ResponseEntity.ok(
            imageGenerationService.searchMyImages(
                    query,
                    page,
                    size
            )
    );
}

    // Get one specific image
    @GetMapping("/{id}")
    public ResponseEntity<ImageGenerationResponse> getImageById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                imageGenerationService.getImageById(id)
        );
    }

    // Delete image
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long id
    ) {

        imageGenerationService.deleteImage(id);

        return ResponseEntity.noContent().build();
    }
}