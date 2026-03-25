package com.hms.prescription.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for creating or updating a prescription.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionRequestDTO {
    
    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    @NotNull(message = "Doctor ID is required")
    private UUID doctorId;

    private UUID appointmentId;

    @Size(max = 1000, message = "Symptoms must not exceed 1000 characters")
    private String symptoms;
    
    @NotBlank(message = "Diagnosis is required")
    @Size(max = 1000, message = "Diagnosis must not exceed 1000 characters")
    private String diagnosis;

    @NotEmpty(message = "At least one medicine is required")
    @Valid
    private List<PrescriptionMedicineRequestDTO> medicines;

    @Size(max = 1000, message = "Advice must not exceed 1000 characters")
    private String advice;
    
    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;
}
