package com.genflow.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ImageGenerationRequest {

    @NotBlank(message = "Prompt is required")
    private String prompt;
}