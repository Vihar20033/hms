package com.hms.security.jwt;

import com.hms.common.enums.TokenType;
import com.hms.user.entity.User;
import com.hms.user.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Objects;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final HandlerExceptionResolver resolver;

    public JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            CustomUserDetailsService userDetailsService,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            if (!jwtUtil.validateToken(jwt)) {
                log.warn("Invalid JWT provided from IP: {}, URL: {}", request.getRemoteAddr(), request.getRequestURI());
                filterChain.doFilter(request, response); 
                return;
            }

            if (jwtUtil.extractTokenType(jwt) != TokenType.ACCESS) {
                log.warn("Attempted to use REFRESH token as ACCESS token by: {}", request.getRemoteAddr());
                filterChain.doFilter(request, response);
                return;
            }

            final String username = jwtUtil.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Fetch user exactly once (using UserDetails which is now our User entity)
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                User user = (User) userDetails;     // Cast to user Entity

                // Validate token version against DB version for instant revocation
                Integer tokenVersion = jwtUtil.extractTokenVersion(jwt);
                if (!Objects.equals(user.getTokenVersion(), tokenVersion)) {
                    log.warn("Stale token detected for {}. Token version: {} | Expected: {}", username, tokenVersion, user.getTokenVersion());
                    filterChain.doFilter(request, response);
                    return;
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.info("Request authorized for user: {} with authorities: {}", username, userDetails.getAuthorities());
            }

        } catch (Exception e) {
            log.error("Authentication filter exception: {}", e.getMessage());
            resolver.resolveException(request, response, null, e);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
