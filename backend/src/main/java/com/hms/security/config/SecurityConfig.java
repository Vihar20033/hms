package com.hms.security.config;

import com.hms.common.config.CorsConfig;
import com.hms.security.jwt.JwtAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

@Slf4j
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtFilter;
        private final CorsConfig corsConfig;
        
        private final HandlerExceptionResolver resolver;

        public SecurityConfig(
                        JwtAuthenticationFilter jwtFilter,
                        CorsConfig corsConfig,
                        @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
                this.jwtFilter = jwtFilter;
                this.corsConfig = corsConfig;
                this.resolver = resolver;
        }

        @Bean
        public WebSecurityCustomizer webSecurityCustomizer() {
                return (web) -> web.ignoring().requestMatchers("/ws-hms/**");
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

                http
                                .cors(cors -> cors.configurationSource(corsConfig.defaultCorsConfigurationSource()))
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers("/ws-hms/**").permitAll()
                                                .requestMatchers("/api/v1/auth/**").permitAll()
                                                .requestMatchers("/api/v1/doctors/available").permitAll()
                                                .requestMatchers("/api/v1/doctors/department/**").permitAll()
                                                .requestMatchers("/api/v1/doctors/specialization/**").permitAll()
                                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                                                .requestMatchers("/error").permitAll()
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        log.error("Unauthorized error at {}: {}", request.getRequestURI(), authException.getMessage());
                                                        resolver.resolveException(request, response, null, authException);
                                                })
                                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                                        log.error("Access denied error at {} for user {}: {}", 
                                                                request.getRequestURI(), 
                                                                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null ? 
                                                                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName() : "Anonymous",
                                                                accessDeniedException.getMessage());
                                                        resolver.resolveException(request, response, null, accessDeniedException);
                                                }));

                http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

                log.info("Security filter chain configured successfully");
                return http.build();
        }
}