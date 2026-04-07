package com.hms.lab.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LabOrderRequest {
    @NotNull
    private Long patientId;
    private Long appointmentId;
    @NotBlank
    private String testName;
    private String notes;
}
