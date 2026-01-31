package com.digitalSansad.auth.security;

import com.digitalSansad.auth.entity.Role;
import com.digitalSansad.auth.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Service
public class JWTService {

  private final JwtKeyProvider keyProvider;

  @Value("${jwt.expiration}")
  private long expiration;

  public JWTService(JwtKeyProvider keyProvider) {
    this.keyProvider = keyProvider;
  }

  public String generateToken(User user) {

    List<String> roles = user.getRoles()
        .stream()
        .map(Role::getName)
        .toList();

    return Jwts.builder()
        .setSubject(user.getId().toString())
        .claim("email", user.getEmail())
        .claim("roles", roles)
        .claim("name", user.getName())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(keyProvider.getPrivateKey(), SignatureAlgorithm.RS256)
        .compact();
  }

  /* ================= TOKEN VALIDATION ================= */

  public boolean isValid(String token) {
    try {
      parseToken(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  /* ================= TOKEN DATA ================= */

  /**
   * Returns userId (subject) from JWT
   */
  public String getUserId(String token) {
    return parseToken(token).getSubject();
  }

  /**
   * Returns email claim from JWT
   */
  public String getEmail(String token) {
    return parseToken(token).get("email", String.class);
  }

  @SuppressWarnings("unchecked")
  public List<String> getRoles(String token) {
    return parseToken(token).get("roles", List.class);
  }

  /* ================= INTERNAL ================= */

  private Claims parseToken(String token) {
    return Jwts.parserBuilder()
        // ✅ RS256 verification uses PUBLIC KEY
        .setSigningKey(keyProvider.getPublicKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

}
