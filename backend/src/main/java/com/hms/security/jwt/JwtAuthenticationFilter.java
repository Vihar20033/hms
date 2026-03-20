package com.hms.security.jwt;

import com.hms.user.entity.User;
import com.hms.user.repository.UserRepository;
import com.hms.user.service.CustomUserDetailsService;
import jakarta.servlet.*;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtUtil.extractUsername(jwt);

        if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            log.debug("Authenticating user: {}", username);
            User user = userRepository.findByUsername(username)
                    .orElse(null);

            if (user == null) {
                log.warn("User not found in database: {}", username);
                filterChain.doFilter(request, response);
                return;
            }

            Integer tokenVersionFromToken =
                    jwtUtil.extractTokenVersion(jwt);

            log.debug("User token version in DB: {}, in token: {}", user.getTokenVersion(), tokenVersionFromToken);
            if (!java.util.Objects.equals(user.getTokenVersion(), tokenVersionFromToken)) {
                log.warn("Token version mismatch for user: {}. Token invalidated.", username);
                filterChain.doFilter(request, response);
                return; // 🔥 Token invalidated
            }

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);

            log.debug("User authorities: {}", userDetails.getAuthorities());
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authenticationToken.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContextHolder.getContext()
                    .setAuthentication(authenticationToken);
            log.info("User {} authenticated successfully via JWT with authorities: {}",
                    username, userDetails.getAuthorities());
        }

        filterChain.doFilter(request, response);
    }
}