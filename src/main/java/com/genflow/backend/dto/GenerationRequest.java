package com.genflow.backend.dto;

import com.genflow.backend.entity.GenerationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerationRequest {

    @NotNull(message = "Generation type is required")
    private GenerationType type;

    @NotBlank(message = "Input is required")
    private String input;
}