package com.digitalSansad.sow.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import java.security.Key;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

  private final JwtPublicKeyProvider keyProvider;

  public JwtAuthFilter(JwtPublicKeyProvider keyProvider) {
    this.keyProvider = keyProvider;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {

    logger.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());

    String authHeader = request.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      logger.debug("Authorization header found, attempting JWT validation");
      String token = authHeader.substring(7);

      try {
        logger.debug("Parsing JWT token");
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(keyProvider.getPublicKey())
            .build()
            .parseClaimsJws(token)
            .getBody();

        logger.info("JWT token validated successfully for subject: {}", claims.getSubject());

        List<String> roles = claims.get("roles", List.class);
        logger.debug("JWT roles extracted: {}", roles);

        var authorities = roles.stream()
            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
            .toList();

        var auth = new UsernamePasswordAuthenticationToken(
            claims.getSubject(),
            null,
            authorities);

        SecurityContextHolder.getContext().setAuthentication(auth);
        logger.info("Security context set with authenticated user: {} and authorities: {}", claims.getSubject(),
            authorities);

      } catch (JwtException e) {
        logger.warn("JWT validation failed: {}", e.getMessage());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }
    } else {
      logger.debug("No Authorization header or invalid format for request: {}", request.getRequestURI());
    }

    filterChain.doFilter(request, response);
  }
}
