package com.hms.auth.controller;

import com.hms.auth.dto.request.ChangePasswordRequest;
import com.hms.auth.dto.request.LoginRequest;
import com.hms.auth.dto.request.RegisterRequest;
import com.hms.auth.dto.request.TokenRefreshRequest;
import com.hms.auth.dto.response.AuthResponse;
import com.hms.auth.service.AuthService;
import com.hms.security.jwt.CookieUtil;
import com.hms.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;
    private final CookieUtil cookieUtil;

    @PostMapping("/register")
    public ApiResponse<String> register(
            @Valid @RequestBody RegisterRequest request) {
        service.register(request);
        return ApiResponse.success("User registered successfully");
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            jakarta.servlet.http.HttpServletResponse response) {
        AuthResponse loginResponse = service.login(request);
        cookieUtil.setAccessTokenCookie(response, loginResponse.getToken());
        cookieUtil.setRefreshTokenCookie(response, loginResponse.getRefreshToken());
        return ApiResponse.success(loginResponse);
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(
            @RequestBody(required = false) TokenRefreshRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest,
            jakarta.servlet.http.HttpServletResponse response) {
        String refreshToken = cookieUtil.getRefreshToken(httpRequest)
                .orElseGet(() -> request != null ? request.getRefreshToken() : null);

        TokenRefreshRequest updatedRequest = TokenRefreshRequest.builder()
                .refreshToken(refreshToken)
                .build();
        AuthResponse refreshResponse = service.refreshToken(updatedRequest);
        
        cookieUtil.setAccessTokenCookie(response, refreshResponse.getToken());
        cookieUtil.setRefreshTokenCookie(response, refreshResponse.getRefreshToken());
        
        return ApiResponse.success(refreshResponse);
    }

    @PostMapping("/change-password")
    public ApiResponse<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        service.changePassword(request);
        return ApiResponse.success("Password changed successfully");
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(jakarta.servlet.http.HttpServletResponse response) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            service.logout(authentication.getName());
        }
        cookieUtil.clearAuthCookies(response);
        return ApiResponse.success("Logged out successfully");
    }

}
