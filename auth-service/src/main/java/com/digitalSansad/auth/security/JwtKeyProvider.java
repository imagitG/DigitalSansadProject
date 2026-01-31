package com.digitalSansad.auth.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
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
      @Value("${jwt.private-key}") Resource privateKeyResource,
      @Value("${jwt.public-key}") Resource publicKeyResource) throws Exception {

    this.privateKey = loadPrivateKey(privateKeyResource);
    this.publicKey = loadPublicKey(publicKeyResource);
  }

  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  public PublicKey getPublicKey() {
    return publicKey;
  }

  /* ================= INTERNAL ================= */

  private PrivateKey loadPrivateKey(Resource resource) throws Exception {
    try (InputStream is = resource.getInputStream()) {
      byte[] keyBytes = decodePem(is.readAllBytes());
      return KeyFactory.getInstance("RSA")
          .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }
  }

  private PublicKey loadPublicKey(Resource resource) throws Exception {
    try (InputStream is = resource.getInputStream()) {
      byte[] keyBytes = decodePem(is.readAllBytes());
      return KeyFactory.getInstance("RSA")
          .generatePublic(new X509EncodedKeySpec(keyBytes));
    }
  }

  private byte[] decodePem(byte[] pem) {
    String content = new String(pem)
        .replaceAll("-----BEGIN (.*)-----", "")
        .replaceAll("-----END (.*)-----", "")
        .replaceAll("\\s", "");
    return Base64.getDecoder().decode(content);
  }
}
