package com.digitalSansad.auth.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class JwtKeyProvider {

  private final PrivateKey privateKey;
  private final PublicKey publicKey;

  public JwtKeyProvider(
      @Value("${jwt.private-key}") String privateKeyValue,
      @Value("${jwt.public-key}") String publicKeyValue) throws Exception {

    this.privateKey = loadPrivateKey(privateKeyValue);
    this.publicKey = loadPublicKey(publicKeyValue);
  }

  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  public PublicKey getPublicKey() {
    return publicKey;
  }

  /* ================= INTERNAL ================= */

  private PrivateKey loadPrivateKey(String value) throws Exception {
    byte[] keyBytes;

    if (value.startsWith("classpath:")) {
      // Load from file
      try (InputStream is = new ClassPathResource(value.replace("classpath:", "")).getInputStream()) {
        keyBytes = decodePem(is.readAllBytes());
      }
    } else {
      // Load from ENV string
      keyBytes = decodePem(value.trim().getBytes());
    }

    return KeyFactory.getInstance("RSA")
        .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
  }

  private PublicKey loadPublicKey(String value) throws Exception {
    byte[] keyBytes;

    if (value.startsWith("classpath:")) {
      // Load from file
      try (InputStream is = new ClassPathResource(value.replace("classpath:", "")).getInputStream()) {
        keyBytes = decodePem(is.readAllBytes());
      }
    } else {
      // Load from ENV string
      keyBytes = decodePem(value.trim().getBytes());
    }

    return KeyFactory.getInstance("RSA")
        .generatePublic(new X509EncodedKeySpec(keyBytes));
  }

  private byte[] decodePem(byte[] pemBytes) {
    String pem = new String(pemBytes);

    // Clean headers
    pem = pem.replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
        .replace("-----BEGIN PUBLIC KEY-----", "")
        .replace("-----END PUBLIC KEY-----", "");

    // Remove ALL whitespace safely
    pem = pem.replaceAll("\\s+", "");

    try {
      return Base64.getDecoder().decode(pem);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Invalid PEM format. Check key encoding.", e);
    }
  }
}