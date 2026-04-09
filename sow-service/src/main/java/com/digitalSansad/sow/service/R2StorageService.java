package com.digitalSansad.sow.service;

import com.digitalSansad.sow.exception.R2StorageException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.exception.SdkException;

@Service
public class R2StorageService {

  private static final Logger logger = LoggerFactory.getLogger(R2StorageService.class);

  private final S3Client s3Client;

  @Value("${r2.bucket}")
  private String bucket;

  public R2StorageService(S3Client s3Client) {
    this.s3Client = s3Client;
    logger.info("R2StorageService initialized");
  }

  public String upload(MultipartFile file, String key) throws IOException {
    logger.info("Starting file upload to R2 - Key: {}, File: {}, Size: {} bytes", key, file.getOriginalFilename(),
        file.getSize());

    if (file == null || file.isEmpty()) {
      logger.error("Upload failed: File is null or empty for key: {}", key);
      throw new R2StorageException("File is null or empty");
    }

    try {
      if (bucket == null || bucket.isEmpty()) {
        logger.error("Upload failed: R2 bucket not configured");
        throw new R2StorageException("R2 bucket configuration missing");
      }

      logger.debug("Creating PutObjectRequest for bucket: {}, key: {}", bucket, key);
      PutObjectRequest request = PutObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .contentType(file.getContentType())
          .build();

      logger.debug("Uploading file bytes to R2");
      s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

      logger.info("File uploaded successfully to R2 - Key: {}", key);
      return key;

    } catch (SdkException e) {
      logger.error("AWS SDK error during file upload - Key: {}, Error: {}", key, e.getMessage(), e);
      throw new R2StorageException("Failed to upload file to R2 storage: " + e.getMessage(), e);
    } catch (IOException e) {
      logger.error("IO error during file upload - Key: {}, Error: {}", key, e.getMessage(), e);
      throw new R2StorageException("IO error reading file for upload: " + e.getMessage(), e);
    } catch (Exception e) {
      logger.error("Unexpected error during file upload - Key: {}, Error: {}", key, e.getMessage(), e);
      throw new R2StorageException("Unexpected error during file upload: " + e.getMessage(), e);
    }
  }

  public ResponseInputStream<GetObjectResponse> download(String key) {
    logger.info("Starting file download from R2 - Key: {}", key);

    if (key == null || key.isEmpty()) {
      logger.error("Download failed: Key is null or empty");
      throw new R2StorageException("File key is null or empty");
    }

    try {
      if (bucket == null || bucket.isEmpty()) {
        logger.error("Download failed: R2 bucket not configured");
        throw new R2StorageException("R2 bucket configuration missing");
      }

      logger.debug("Creating GetObjectRequest for bucket: {}, key: {}", bucket, key);
      ResponseInputStream<GetObjectResponse> response = s3Client.getObject(GetObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .build());

      logger.info("File download stream created successfully from R2 - Key: {}", key);
      return response;

    } catch (SdkException e) {
      logger.error("AWS SDK error during file download - Key: {}, Error: {}", key, e.getMessage(), e);
      throw new R2StorageException("Failed to download file from R2 storage: " + e.getMessage(), e);
    } catch (Exception e) {
      logger.error("Unexpected error during file download - Key: {}, Error: {}", key, e.getMessage(), e);
      throw new R2StorageException("Unexpected error during file download: " + e.getMessage(), e);
    }
  }

  public void delete(String key) {
    logger.info("Starting file deletion from R2 - Key: {}", key);

    if (key == null || key.isEmpty()) {
      logger.error("Delete failed: Key is null or empty");
      throw new R2StorageException("File key is null or empty");
    }

    try {
      if (bucket == null || bucket.isEmpty()) {
        logger.error("Delete failed: R2 bucket not configured");
        throw new R2StorageException("R2 bucket configuration missing");
      }

      logger.debug("Creating DeleteObjectRequest for bucket: {}, key: {}", bucket, key);
      s3Client.deleteObject(DeleteObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .build());

      logger.info("File deleted successfully from R2 - Key: {}", key);

    } catch (SdkException e) {
      logger.error("AWS SDK error during file deletion - Key: {}, Error: {}", key, e.getMessage(), e);
      throw new R2StorageException("Failed to delete file from R2 storage: " + e.getMessage(), e);
    } catch (Exception e) {
      logger.error("Unexpected error during file deletion - Key: {}, Error: {}", key, e.getMessage(), e);
      throw new R2StorageException("Unexpected error during file deletion: " + e.getMessage(), e);
    }
  }
}