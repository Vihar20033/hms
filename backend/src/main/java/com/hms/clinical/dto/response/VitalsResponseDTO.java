package com.hms.clinical.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class VitalsResponseDTO {
    private UUID id;
    private UUID appointmentId;
    private BigDecimal temperature;
    private Integer systolicBP;
    private Integer diastolicBP;
    private Integer pulseRate;
    private Integer respiratoryRate;
    private BigDecimal spo2;
    private BigDecimal weight;
    private BigDecimal height;
    private String notes;
    private LocalDateTime createdAt;
}
