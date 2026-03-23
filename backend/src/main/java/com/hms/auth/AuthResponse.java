package com.hms.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String refreshToken;
    private String username;
    private String email;
    private String role;
    private Boolean passwordChangeRequired;
}
