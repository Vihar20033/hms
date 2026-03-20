package com.hms.doctor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponseDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String specialization;
    private String registrationNumber;
    private String department;
    private String email;
    private String contactNumber;
    private String bio;
    private String qualification;
    private Integer experienceYears;
    private String licenseNumber;
    private java.math.BigDecimal consultationFee;
    private Boolean isAvailable;
    private String phoneNumber;
    private String designation;
}
