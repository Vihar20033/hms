package com.hms.doctor.dto.request;

import com.hms.common.validation.ValidationPatterns;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDoctorRequest {

    private Long userId;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = ValidationPatterns.USERNAME, message = "Username may contain letters, numbers, dot, underscore, or hyphen only")
    private String username;

    @NotBlank(message = "Temporary password is required")
    @Size(min = 8, max = 128, message = "Temporary password must be between 8 and 128 characters")
    @Pattern(regexp = ValidationPatterns.STRONG_PASSWORD, message = "Temporary password must contain uppercase, lowercase, number, and special character")
    private String temporaryPassword;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Pattern(regexp = ValidationPatterns.PERSON_NAME, message = "First name contains invalid characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Pattern(regexp = ValidationPatterns.PERSON_NAME, message = "Last name contains invalid characters")
    private String lastName;

    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    @NotBlank(message = "Specialization is required")
    @Size(max = 100, message = "Specialization must not exceed 100 characters")
    private String specialization;

    @Size(max = 200, message = "Qualification must not exceed 200 characters")
    private String qualification;

    @Min(value = 0, message = "Experience cannot be negative")
    @Max(value = 100, message = "Experience cannot exceed 100 years")
    private Integer experienceYears;

    @NotBlank(message = "License number is required")
    @Size(max = 50, message = "License number must not exceed 50 characters")
    @Pattern(regexp = ValidationPatterns.LICENSE_NUMBER, message = "License number format is invalid")
    private String licenseNumber;

    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;

    @NotNull(message = "Consultation fee is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Consultation fee must be positive")
    @Digits(integer = 10, fraction = 2, message = "Consultation fee must have up to 2 decimal places")
    private BigDecimal consultationFee;

    @Builder.Default
    private Boolean isAvailable = true;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Pattern(regexp = ValidationPatterns.PHONE, message = "Phone number must contain 10 to 15 digits and may start with +")
    private String phoneNumber;

    @Size(max = 100, message = "Designation must not exceed 100 characters")
    private String designation;
}
