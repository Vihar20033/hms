package com.hms.prescription.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


/**
 * Data Transfer Object representing the response details of a prescription.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionResponseDTO {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private Long appointmentId;
    private String symptoms;
    private String diagnosis;
    private List<PrescriptionMedicineResponseDTO> medicines;
    private String advice;
    private String notes;
    private LocalDateTime createdAt;
}
