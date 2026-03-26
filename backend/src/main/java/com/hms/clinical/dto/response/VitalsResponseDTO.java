package com.hms.clinical.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class VitalsResponseDTO {
    private Long id;
    private Long appointmentId;
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
