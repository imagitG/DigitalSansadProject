package com.digitalSansad.sow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class JwtPublicKeyProvider {

  private static final Logger logger = LoggerFactory.getLogger(JwtPublicKeyProvider.class);

  private final PublicKey publicKey;

  public JwtPublicKeyProvider(
      @Value("${jwt.public-key}") String publicKeyValue) throws Exception {

    logger.info("Initializing JwtPublicKeyProvider");
    logger.debug("Public key value source: {}",
        publicKeyValue.startsWith("classpath:") ? "CLASSPATH" : "ENVIRONMENT_VARIABLE");
    this.publicKey = loadPublicKey(publicKeyValue);
    logger.info("JwtPublicKeyProvider initialized successfully");
  }

  public PublicKey getPublicKey() {
    return publicKey;
  }

  /* ================= INTERNAL ================= */

  private PublicKey loadPublicKey(String value) throws Exception {
    logger.debug("Loading public key");
    byte[] keyBytes;

    if (value.startsWith("classpath:")) {
      // ✅ Load from file (local dev)
      String resourcePath = value.replace("classpath:", "");
      logger.info("Loading public key from classpath: {}", resourcePath);
      try (InputStream is = new ClassPathResource(resourcePath).getInputStream()) {
        keyBytes = decodePem(is.readAllBytes());
        logger.info("Successfully loaded public key from classpath file");
      } catch (Exception e) {
        logger.error("Failed to load public key from classpath: {}", resourcePath, e);
        throw e;
      }
    } else {
      // ✅ Load from ENV (Render)
      logger.info("Loading public key from environment variable");
      try {
        keyBytes = decodePem(value.trim().getBytes());
        logger.info("Successfully loaded public key from environment variable");
      } catch (Exception e) {
        logger.error("Failed to load public key from environment variable", e);
        throw e;
      }
    }

    logger.debug("Generating RSA public key from decoded bytes");
    return KeyFactory.getInstance("RSA")
        .generatePublic(new X509EncodedKeySpec(keyBytes));
  }

  private byte[] decodePem(byte[] pemBytes) {
    logger.debug("Decoding PEM format");
    String pem = new String(pemBytes);

    pem = pem.replace("-----BEGIN PUBLIC KEY-----", "")
        .replace("-----END PUBLIC KEY-----", "")
        .replaceAll("\\s+", "");

    try {
      byte[] decoded = Base64.getDecoder().decode(pem);
      logger.debug("Successfully decoded PEM, bytes length: {}", decoded.length);
      return decoded;
    } catch (IllegalArgumentException e) {
      logger.error("Invalid PEM format: {}", e.getMessage());
      throw new RuntimeException("Invalid PEM format. Check public key.", e);
    }
  }
}