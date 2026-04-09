package com.digitalSansad.sow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Configuration
public class CorsConfig {

        private static final Logger logger = LoggerFactory.getLogger(CorsConfig.class);

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {

                logger.info("Configuring CORS");

                CorsConfiguration config = new CorsConfiguration();

                config.setAllowedOrigins(List.of("http://localhost:3000", "https://digital-sansad-project.vercel.app"));
                logger.debug("CORS allowed origins: {}", config.getAllowedOrigins());

                config.setAllowedMethods(List.of(
                                "GET", "POST", "PUT", "DELETE", "OPTIONS"));
                logger.debug("CORS allowed methods: {}", config.getAllowedMethods());

                config.setAllowedHeaders(List.of(
                                "Authorization",
                                "Content-Type"));
                logger.debug("CORS allowed headers: {}", config.getAllowedHeaders());

                config.setExposedHeaders(List.of(
                                "Authorization"));
                logger.debug("CORS exposed headers: {}", config.getExposedHeaders());

                config.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                source.registerCorsConfiguration("/**", config);
                logger.info("CORS configuration completed and registered for all paths");
                return source;
        }

        @Bean
        public RestTemplate restTemplate() {
                return new RestTemplate();
        }
}
