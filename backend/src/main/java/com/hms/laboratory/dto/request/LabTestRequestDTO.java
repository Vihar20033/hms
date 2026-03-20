package com.hms.laboratory.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hms.common.validation.ValidationPatterns;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LabTestRequestDTO {
    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    private UUID doctorId;

    @NotBlank(message = "Test name is required")
    @Size(max = 200, message = "Test name must not exceed 200 characters")
    private String testName;

    @NotBlank(message = "Test code is required")
    @Size(max = 50, message = "Test code must not exceed 50 characters")
    @Pattern(regexp = ValidationPatterns.CODE, message = "Test code format is invalid")
    private String testCode;

    @NotNull(message = "Test price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Price must not exceed 999999.99")
    private BigDecimal price;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    private UUID appointmentId;

    @JsonCreator
    public static LabTestRequestDTO create(
            @JsonProperty("patientId") String patientId,
            @JsonProperty("doctorId") String doctorId,
            @JsonProperty("appointmentId") String appointmentId,
            @JsonProperty("testName") String testName,
            @JsonProperty("testCode") String testCode,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("description") String description,
            @JsonProperty("category") String category) {
        LabTestRequestDTO dto = new LabTestRequestDTO();
        dto.patientId = patientId != null ? UUID.fromString(patientId) : null;
        dto.doctorId = doctorId != null ? UUID.fromString(doctorId) : null;
        dto.appointmentId = appointmentId != null ? UUID.fromString(appointmentId) : null;
        dto.testName = testName;
        dto.testCode = testCode;
        dto.price = price;
        dto.description = description;
        dto.category = category;
        return dto;
    }
}


