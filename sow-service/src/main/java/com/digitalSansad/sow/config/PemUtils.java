package com.digitalSansad.sow.config;

public class PemUtils {

  public static byte[] parsePEM(byte[] pem) {
    String content = new String(pem)
        .replaceAll("-----BEGIN (.*)-----", "")
        .replaceAll("-----END (.*)-----", "")
        .replaceAll("\\s", "");
    return java.util.Base64.getDecoder().decode(content);
  }
}
