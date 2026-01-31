package com.digitalSansad.sow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.io.InputStream;
import java.nio.file.Files;

@Component
public class JwtPublicKeyProvider {

  private final PublicKey publicKey;

  public JwtPublicKeyProvider(
      @Value("${jwt.public-key}") Resource publicKeyResource) throws Exception {

    this.publicKey = loadPublicKey(publicKeyResource);
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

  public PublicKey getPublicKey() {
    return publicKey;
  }
}
