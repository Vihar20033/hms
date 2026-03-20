package com.hms.doctor.dto.request;

import com.hms.common.validation.ValidationPatterns;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request DTO for updating an existing doctor
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDoctorRequest {

    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Pattern(regexp = ValidationPatterns.PERSON_NAME, message = "First name contains invalid characters")
    private String firstName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Pattern(regexp = ValidationPatterns.PERSON_NAME, message = "Last name contains invalid characters")
    private String lastName;

    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    @Size(max = 100, message = "Specialization must not exceed 100 characters")
    private String specialization;

    @Size(max = 200, message = "Qualification must not exceed 200 characters")
    private String qualification;

    @Min(value = 0, message = "Experience cannot be negative")
    @Max(value = 100, message = "Experience cannot exceed 100 years")
    private Integer experienceYears;

    @Size(max = 50, message = "License number must not exceed 50 characters")
    @Pattern(regexp = ValidationPatterns.LICENSE_NUMBER, message = "License number format is invalid")
    private String licenseNumber;

    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;

    @DecimalMin(value = "0.0", inclusive = false, message = "Consultation fee must be positive")
    @Digits(integer = 10, fraction = 2, message = "Consultation fee must have up to 2 decimal places")
    private BigDecimal consultationFee;

    private Boolean isAvailable;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Pattern(regexp = ValidationPatterns.PHONE, message = "Phone number must contain 10 to 15 digits and may start with +")
    private String phoneNumber;

    @Size(max = 100, message = "Designation must not exceed 100 characters")
    private String designation;

    private String profileImageUrl;
}
