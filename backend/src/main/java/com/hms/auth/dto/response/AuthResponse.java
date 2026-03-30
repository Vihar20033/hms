package com.hms.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    @JsonIgnore
    private String token;
    @JsonIgnore
    private String refreshToken;
    private String username;
    private String email;
    private String role;
    private Boolean passwordChangeRequired;
}
