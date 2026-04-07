package com.hms.lab.dto.response;

import com.hms.common.enums.LabOrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LabOrderResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long appointmentId;
    private String testName;
    private LabOrderStatus status;
    private String resultSummary;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
