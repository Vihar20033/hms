package com.hms.laboratory.dto.response;

import com.hms.common.enums.TestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabTestResponseDTO {
    private UUID id;
    private String testName;
    private String testCode;
    private String category;
    private BigDecimal price;
    private UUID patientId;
    private String patientName;
    private UUID doctorId;
    private String doctorName;
    private TestStatus status;
    private LocalDateTime requestedDate;
    private LocalDateTime completedDate;
}

