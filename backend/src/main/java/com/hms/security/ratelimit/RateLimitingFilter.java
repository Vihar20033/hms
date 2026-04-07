package com.hms.security.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.common.exception.HmsErrorCode;
import com.hms.common.response.ApiError;
import com.hms.security.config.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final ProxyManager<String> proxyManager;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !properties.isEnabled()
                || "OPTIONS".equalsIgnoreCase(request.getMethod())
                || request.getRequestURI().startsWith("/actuator/health");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String key = resolveClientKey(request);
        Bucket bucket = proxyManager.builder().build(key, getBucketConfigurationSupplier());
        
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(Math.max(0, probe.getRemainingTokens())));

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(1, Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiError error = ApiError.of(
                "Too many requests. Please retry after " + retryAfterSeconds + " seconds.",
                HmsErrorCode.RATE_LIMIT_EXCEEDED.getCode(),
                HttpStatus.TOO_MANY_REQUESTS
        );
        error.setPath(request.getRequestURI());
        objectMapper.writeValue(response.getOutputStream(), error);
    }

    private Supplier<BucketConfiguration> getBucketConfigurationSupplier() {
        return () -> {
            Refill refill = Refill.intervally(
                    properties.getRefillTokens(),
                    Duration.ofMinutes(properties.getRefillMinutes())
            );
            Bandwidth limit = Bandwidth.classic(properties.getCapacity(), refill);
            return BucketConfiguration.builder()
                    .addLimit(limit)
                    .build();
        };
    }

    private String resolveClientKey(HttpServletRequest request) {
        String user = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : null;
        if (user != null && !user.isBlank()) {
            return "user:" + user;
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return "ip:" + forwardedFor.split(",")[0].trim();
        }
        return "ip:" + request.getRemoteAddr();
    }
}
