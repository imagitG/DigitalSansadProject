package com.digitalSansad.sow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class R2Config {

  private static final Logger logger = LoggerFactory.getLogger(R2Config.class);

  private final R2Properties r2Properties;

  public R2Config(R2Properties r2Properties) {
    this.r2Properties = r2Properties;
  }

  @Bean
  public S3Client s3Client() {
    logger.info("Initializing R2 S3Client");

    String endpoint = r2Properties.getEndpoint();
    String accessKey = r2Properties.getAccessKey();
    String secretKey = r2Properties.getSecretKey();

    if (endpoint == null || endpoint.isEmpty()) {
      logger.warn("R2 endpoint not configured");
    }

    if (accessKey == null || accessKey.isEmpty()) {
      logger.warn("R2 access key not configured");
    }

    if (secretKey == null || secretKey.isEmpty()) {
      logger.warn("R2 secret key not configured");
    }

    S3Client client = S3Client.builder()
        .endpointOverride(URI.create(endpoint))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)))
        .region(Region.of("auto"))
        .build();

    logger.info("R2 S3Client initialized successfully with endpoint: {}", endpoint);
    return client;
  }
}