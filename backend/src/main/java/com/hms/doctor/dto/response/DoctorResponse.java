package com.hms.doctor.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * Response DTO for doctor data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorResponse {

    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String department;
    private String specialization;
    private String qualification;
    private Integer experienceYears;
    private String licenseNumber;
    private String bio;
    private BigDecimal consultationFee;
    private Boolean isAvailable;
    private String profileImageUrl;
    private String phoneNumber;
    private String designation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
