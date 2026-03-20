package com.hms.laboratory.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabReportRequestDTO {
    
    @NotNull(message = "Lab test ID is required")
    private UUID labTestId;
    
    @NotBlank(message = "Test result/findings are required")
    @Size(min = 5, max = 5000, message = "Findings must be between 5 and 5000 characters")
    private String findings;
    
    @Size(max = 1000, message = "Result must not exceed 1000 characters")
    private String result;
    
    @Size(max = 100, message = "Unit must not exceed 100 characters")
    private String unit;
    
    @Size(max = 200, message = "Reference range must not exceed 200 characters")
    private String referenceRange;
    
    @Size(max = 1000, message = "Remarks must not exceed 1000 characters")
    private String remarks;
    
    @NotBlank(message = "Performed by (staff name) is required")
    @Size(max = 200, message = "Performed by must not exceed 200 characters")
    private String performedBy;
}
