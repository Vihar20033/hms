package com.hms.auth.controller;

import com.hms.auth.dto.request.ChangePasswordRequest;
import com.hms.auth.dto.request.LoginRequest;
import com.hms.auth.dto.request.RegisterRequest;
import com.hms.auth.dto.request.TokenRefreshRequest;
import com.hms.auth.dto.response.AuthResponse;
import com.hms.auth.service.AuthService;
import com.hms.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/register")
    public ApiResponse<String> register(
            @Valid @RequestBody RegisterRequest request) {
        service.register(request);
        return ApiResponse.success("User registered successfully");
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(service.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(
            @Valid @RequestBody TokenRefreshRequest request) {
        return ApiResponse.success(service.refreshToken(request));
    }

    @PostMapping("/change-password")
    public ApiResponse<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        service.changePassword(request);
        return ApiResponse.success("Password changed successfully");
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout() {
        String username = Objects.requireNonNull(SecurityContextHolder
                .getContext()
                .getAuthentication())
                .getName();
        service.logout(username);
        return ApiResponse.success("Logged out successfully");
    }

}
