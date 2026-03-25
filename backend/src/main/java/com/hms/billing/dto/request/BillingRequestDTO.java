package com.hms.billing.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hms.common.config.LocalDateTimeDeserializer;
import com.hms.common.enums.PaymentMethod;
import com.hms.common.enums.PaymentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingRequestDTO {

    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    private UUID appointmentId;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be positive")
    @Digits(integer = 10, fraction = 2, message = "Total amount must have up to 2 decimal places")
    private BigDecimal totalAmount;

    @DecimalMin(value = "0.0", message = "Tax amount must be non-negative")
    @Digits(integer = 10, fraction = 2, message = "Tax amount must have up to 2 decimal places")
    private BigDecimal taxAmount;
    @DecimalMin(value = "0.0", message = "Discount amount must be non-negative")
    @Digits(integer = 10, fraction = 2, message = "Discount amount must have up to 2 decimal places")
    private BigDecimal discountAmount;

    @NotNull(message = "Net amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Net amount must be positive")
    @Digits(integer = 10, fraction = 2, message = "Net amount must have up to 2 decimal places")
    private BigDecimal netAmount;

    @NotNull(message = "Payment status is required")
    private PaymentStatus paymentStatus;

    private PaymentMethod paymentMethod;

    @NotNull(message = "Billing date is required")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime billingDate;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime dueDate;
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    @NotEmpty(message = "Billing items cannot be empty")
    @Valid
    private List<BillingItemRequestDTO> items;

    private String insuranceProvider;
    private String insuranceClaimNumber;
    private BigDecimal insuranceAmount;
    private String insuranceStatus;

    @AssertTrue(message = "Due date must be on or after billing date")
    public boolean isDueDateValid() {
        if (billingDate == null || dueDate == null) {
            return true;
        }
        return !dueDate.isBefore(billingDate);
    }

}
