package com.hms.lab.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LabResultRequest {
    @NotBlank
    private String resultSummary;
    private String notes;
}
