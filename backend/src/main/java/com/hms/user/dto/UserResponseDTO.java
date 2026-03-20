package com.hms.user.dto;

import com.hms.common.enums.Role;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private UUID id;
    private String username;
    private String email;
    private Role role;
    private Boolean enabled;
}

