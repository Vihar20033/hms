package com.hms.clinical.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;


@Data
public class VitalsRequestDTO {
    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;
    
    @DecimalMin(value = "30.0", message = "Temperature must be at least 30 C")
    @DecimalMax(value = "45.0", message = "Temperature must not exceed 45 C")
    private BigDecimal temperature;
    @Min(value = 50, message = "Systolic BP must be at least 50")
    @Max(value = 250, message = "Systolic BP must not exceed 250")
    private Integer systolicBP;
    @Min(value = 30, message = "Diastolic BP must be at least 30")
    @Max(value = 150, message = "Diastolic BP must not exceed 150")
    private Integer diastolicBP;
    @Min(value = 30, message = "Pulse rate must be at least 30")
    @Max(value = 250, message = "Pulse rate must not exceed 250")
    private Integer pulseRate;
    @Min(value = 5, message = "Respiratory rate must be at least 5")
    @Max(value = 60, message = "Respiratory rate must not exceed 60")
    private Integer respiratoryRate;
    @DecimalMin(value = "50.0", message = "SpO2 must be at least 50")
    @DecimalMax(value = "100.0", message = "SpO2 must not exceed 100")
    private BigDecimal spo2;
    @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be positive")
    @DecimalMax(value = "500.0", message = "Weight must not exceed 500 kg")
    private BigDecimal weight;
    @DecimalMin(value = "0.0", inclusive = false, message = "Height must be positive")
    @DecimalMax(value = "300.0", message = "Height must not exceed 300 cm")
    private BigDecimal height;
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    @AssertTrue(message = "Systolic BP must be greater than diastolic BP")
    public boolean isBloodPressureValid() {
        if (systolicBP == null || diastolicBP == null) {
            return true;
        }
        return systolicBP > diastolicBP;
    }
}
