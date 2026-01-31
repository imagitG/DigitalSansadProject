package com.digitalSansad.auth.security;

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

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

  private final JWTService jwtService;

  public JWTAuthenticationFilter(JWTService jwtService) {
    this.jwtService = jwtService;
  }

  private static final List<String> EXCLUDED_PATHS = List.of(
      "/auth/"
  // "/admin/"
  );

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();

    return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
      // if request is unauthenticated, let it continue to next filter/function
      // (because not all endpoints may need authentication)
    }

    String token = authHeader.substring(7);

    try {
      if (!jwtService.isValid(token)) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }

      // Extract userId as the principal (stored in JWT subject)
      String userId = jwtService.getUserId(token);

      List<String> roles = jwtService.getRoles(token);

      System.out.println("\n\nJWT ROLES: " + roles + "\n\n");

      List<SimpleGrantedAuthority> authorities = roles.stream()
          .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
          .toList();
      System.out.println("\n\n created authority " + "\n\n");

      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          userId,
          null,
          authorities);

      System.out.println("\n\n created authentication " + "\n\n");

      SecurityContextHolder.getContext().setAuthentication(authentication);
      System.out.println("\n\n set authentication in security context " + "\n\n");

    } catch (Exception ex) {
      System.out.println("\n\nException : " + ex.getMessage() + "\n\n");
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    filterChain.doFilter(request, response);
  }
}
