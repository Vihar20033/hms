package com.hms.prescription.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionMedicineRequestDTO {
    
    @NotBlank(message = "Medicine name is required")
    @Size(max = 200, message = "Medicine name must not exceed 200 characters")
    private String medicineName;
    
    @NotBlank(message = "Dosage is required")
    @Size(max = 200, message = "Dosage must not exceed 200 characters")
    private String dosage;
    
    @NotBlank(message = "Duration is required")
    @Size(max = 100, message = "Duration must not exceed 100 characters")
    private String duration;
    
    @NotNull(message = "Quantity is required")
    /** Fix #5 - Long quantity following inventory overflow fix */
    private Long quantity;
    
    @Size(max = 500, message = "Instructions must not exceed 500 characters")
    private String instructions;
}
