package com.genflow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CloudinaryUploadResult {

    private String imageUrl;
    private String publicId;
}