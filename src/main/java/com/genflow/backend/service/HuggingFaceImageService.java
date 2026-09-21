package com.genflow.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class HuggingFaceImageService {

    private final RestClient restClient;
    private final String token;

    public HuggingFaceImageService(
            @Value("${huggingface.api.token}") String token
    ) {
        this.restClient = RestClient.builder()
                .baseUrl("https://router.huggingface.co")
                .build();

        this.token = token;
    }

    public byte[] generateImage(String prompt) {

        Map<String, Object> requestBody = Map.of(
                "inputs", prompt
        );

        byte[] imageBytes = restClient.post()
                .uri("/hf-inference/models/stabilityai/stable-diffusion-3-medium-diffusers")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.IMAGE_PNG)
                .body(requestBody)
                .retrieve()
                .body(byte[].class);

        if (imageBytes == null || imageBytes.length == 0) {
            throw new RuntimeException(
                    "Hugging Face returned an empty image"
            );
        }

        return imageBytes;
    }
}