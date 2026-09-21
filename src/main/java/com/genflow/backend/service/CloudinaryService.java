package com.genflow.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.genflow.backend.dto.CloudinaryUploadResult;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret
    ) {

        this.cloudinary = new Cloudinary(
                ObjectUtils.asMap(
                        "cloud_name", cloudName,
                        "api_key", apiKey,
                        "api_secret", apiSecret,
                        "secure", true
                )
        );
    }

    public CloudinaryUploadResult uploadImage(byte[] imageBytes) {

        try {

            Map uploadResult = cloudinary.uploader().upload(
                    imageBytes,
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "folder", "genflow"
                    )
            );

            Object secureUrl = uploadResult.get("secure_url");
            Object publicId = uploadResult.get("public_id");

            if (secureUrl == null || publicId == null) {
                throw new RuntimeException(
                        "Cloudinary did not return image information"
                );
            }

            return new CloudinaryUploadResult(
                    secureUrl.toString(),
                    publicId.toString()
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to upload image to Cloudinary",
                    e
            );
        }
    }

    public void deleteImage(String publicId) {

    try {

        Map result = cloudinary.uploader().destroy(
                publicId,
                ObjectUtils.asMap(
                        "resource_type", "image",
                        "invalidate", true
                )
        );

        System.out.println(
                "Cloudinary delete result: " + result
        );

    } catch (IOException e) {

        throw new RuntimeException(
                "Failed to delete image from Cloudinary",
                e
        );
    }
}
}