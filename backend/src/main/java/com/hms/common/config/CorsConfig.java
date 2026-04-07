package com.hms.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class CorsConfig {

        @Value("${hms.frontend.allowed-origins:http://localhost:4200,http://127.0.0.1:4200}")
        private String allowedOrigins;

        @Bean
        public CorsConfigurationSource defaultCorsConfigurationSource() {

                CorsConfiguration configuration = new CorsConfiguration();

                List<String> origins = Arrays.stream(allowedOrigins.split(","))
                                .map(String::trim)
                                .filter(origin -> !origin.isBlank())
                                .toList();
                configuration.setAllowedOrigins(origins);

                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                configuration.setAllowedHeaders(
                                Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));
                configuration.setExposedHeaders(Collections.singletonList("Authorization"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
