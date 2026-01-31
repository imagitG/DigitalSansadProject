package com.digitalSansad.sow.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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

  private final JwtPublicKeyProvider keyProvider;

  public JwtAuthFilter(JwtPublicKeyProvider keyProvider) {
    this.keyProvider = keyProvider;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {

    String authHeader = request.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);

      try {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(keyProvider.getPublicKey())
            .build()
            .parseClaimsJws(token)
            .getBody();

        List<String> roles = claims.get("roles", List.class);

        var authorities = roles.stream()
            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
            .toList();

        var auth = new UsernamePasswordAuthenticationToken(
            claims.getSubject(),
            null,
            authorities);

        SecurityContextHolder.getContext().setAuthentication(auth);

      } catch (JwtException e) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }
    }

    filterChain.doFilter(request, response);
  }
}
