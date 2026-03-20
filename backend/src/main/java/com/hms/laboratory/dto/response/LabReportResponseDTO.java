package com.hms.laboratory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabReportResponseDTO {
    
    private UUID id;
    private UUID labTestId;
    private String testName;
    private String testCode;
    private String patientName;
    private String findings;
    private String result;
    private String unit;
    private String referenceRange;
    private String remarks;
    private String performedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
