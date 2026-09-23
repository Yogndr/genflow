package com.genflow.backend.service;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
public class S3StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public S3StorageService(
            S3Client s3Client,
            S3Presigner s3Presigner
    ) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    // Upload a general file to S3
    public String uploadFile(MultipartFile file) throws IOException {

        String key =
                "uploads/" + UUID.randomUUID()
                        + "-" + file.getOriginalFilename();

        PutObjectRequest request =
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(file.getContentType())
                        .build();

        s3Client.putObject(
                request,
                RequestBody.fromBytes(file.getBytes())
        );

        return key;
    }

    // Upload an AI-generated image to S3
    public String uploadImage(byte[] imageBytes) {

        String key =
                "generated-images/" + UUID.randomUUID() + ".png";

        PutObjectRequest request =
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType("image/png")
                        .build();

        s3Client.putObject(
                request,
                RequestBody.fromBytes(imageBytes)
        );

        return key;
    }

    // Download a file from S3
    public byte[] downloadFile(String key) {

        GetObjectRequest request =
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

        ResponseBytes<GetObjectResponse> object =
                s3Client.getObjectAsBytes(request);

        return object.asByteArray();
    }

    // Generate temporary URL for a private S3 object
    public String generatePresignedUrl(String key) {

        GetObjectRequest getObjectRequest =
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(15))
                        .getObjectRequest(getObjectRequest)
                        .build();

        return s3Presigner
                .presignGetObject(presignRequest)
                .url()
                .toString();
    }

    // Delete a file from S3
    public void deleteFile(String key) {

        DeleteObjectRequest request =
                DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

        s3Client.deleteObject(request);
    }
}