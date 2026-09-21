package com.genflow.backend.dto;

import com.genflow.backend.entity.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GenerationResponse {

    private Long id;
    private GenerationType type;
    private String input;
    private String output;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}