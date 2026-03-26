package com.hms.prescription.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



/**
 * Data Transfer Object for medicine details within a prescription response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionMedicineResponseDTO {
    private Long id;
    private String medicineName;
    private String dosage;
    private String duration;
    private Integer quantity;
    private String instructions;
}
