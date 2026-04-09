package com.digitalSansad.sow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http,
      JwtAuthFilter jwtFilter) throws Exception {

    logger.info("Configuring security filter chain");

    http
        .cors(cors -> {
          logger.debug("CORS enabled");
        }) // ✅ ENABLE CORS
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> {
          logger.debug("Configuring authorization rules");
          auth
              .requestMatchers("/sow/**").authenticated()
              .anyRequest().permitAll();
          logger.debug("Authorization rules: /sow/** requires authentication, all others permit all");
        })
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    logger.info("Security filter chain configured successfully with JWT authentication filter");
    return http.build();
  }
}
