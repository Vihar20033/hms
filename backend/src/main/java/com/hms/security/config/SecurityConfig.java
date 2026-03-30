package com.hms.security.config;

import com.hms.common.config.CorsConfig;
import com.hms.common.enums.Role;
import com.hms.security.jwt.JwtAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Slf4j
@Configuration
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
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfig.defaultCorsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                
                // Add Security Headers for hardening
                .headers(headers -> headers
                    .frameOptions(Customizer.withDefaults()) // Deny by default in modern Spring Security (SpringBoot 3+)
                    .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                    .xssProtection(Customizer.withDefaults())
                    .contentTypeOptions(Customizer.withDefaults())
                )

                .authorizeHttpRequests(auth -> auth
                        // Public Endpoints
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/api/v1/auth/logout", "/api/v1/auth/change-password").authenticated()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/**").hasRole(Role.ADMIN.name())
                        .requestMatchers("/error").permitAll()

                        // Role-Based Endpoint Configuration Example (Preferred to be fine-grained using @PreAuthorize)
                        .requestMatchers("/api/v1/admin/**").hasRole(Role.ADMIN.name())
                        .requestMatchers("/api/v1/pharmacy/**").hasAnyRole(Role.ADMIN.name(), Role.PHARMACIST.name())

                        // Anything else must be authenticated
                        .anyRequest().authenticated())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(exceptions -> exceptions
                        // Handle 401 Unauthorized securely
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("Access unauthorized to {} - {}", request.getRequestURI(), authException.getMessage());
                            resolver.resolveException(request, response, null, authException);
                        })
                        // Handle 403 Forbidden securely
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.error("Access forbidden to {} for user {}", request.getRequestURI(), 
                                SecurityContextHolder.getContext().getAuthentication().getName());
                            resolver.resolveException(request, response, null, accessDeniedException);
                        }));

        // Insert custom filters
        // http.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("Production Security configuration successfully initialized.");
        return http.build();
    }
}
