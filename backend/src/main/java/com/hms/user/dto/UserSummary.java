package com.hms.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simplified user details for dropdowns and listings
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSummary {
    private String id;
    private String username;
}
