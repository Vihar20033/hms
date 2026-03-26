package com.hms.user.dto;

import com.hms.common.enums.Role;
import lombok.*;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private Boolean enabled;
}

