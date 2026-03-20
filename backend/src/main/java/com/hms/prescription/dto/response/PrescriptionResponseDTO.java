package com.hms.prescription.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionResponseDTO {
    private UUID id;
    private UUID patientId;
    private String patientName;
    private UUID doctorId;
    private String doctorName;
    private UUID appointmentId;
    private String symptoms;
    private String diagnosis;
    private List<PrescriptionMedicineResponseDTO> medicines;
    private String advice;
    private String notes;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrescriptionMedicineResponseDTO {
        private UUID id;
        private String medicineName;
        private String dosage;
        private String duration;
        private Integer quantity;
        private String instructions;
    }
}
