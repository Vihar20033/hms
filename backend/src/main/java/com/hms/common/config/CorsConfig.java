package com.hms.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;


import java.util.Arrays;
import java.util.Collections;

@Configuration
public class CorsConfig {

        @Bean
        public CorsConfigurationSource defaultCorsConfigurationSource() {

                CorsConfiguration configuration = new CorsConfiguration();

                // In development, we allow common localhost ports used by Angular/React/Vite
                configuration.setAllowedOrigins(Arrays.asList(
                                "http://localhost:4200",
                                "http://localhost:3000",
                                "http://localhost:8080",
                                "http://localhost:8083",
                                "http://127.0.0.1:4200"));

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