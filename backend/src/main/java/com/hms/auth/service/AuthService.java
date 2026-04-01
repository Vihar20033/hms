package com.hms.auth.service;

import com.hms.auth.dto.request.ChangePasswordRequest;
import com.hms.auth.dto.request.LoginRequest;
import com.hms.auth.dto.request.RegisterRequest;
import com.hms.auth.dto.request.TokenRefreshRequest;
import com.hms.auth.dto.response.AuthResponse;

public interface AuthService {
    void register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(TokenRefreshRequest request);
    void changePassword(ChangePasswordRequest request);
    void logout(String username, String refreshToken);
}
