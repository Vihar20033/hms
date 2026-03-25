package com.hms.billing.dto.response;

import com.hms.common.enums.PaymentMethod;
import com.hms.common.enums.PaymentStatus;
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
public class BillingResponseDTO {
    private UUID id;
    private String invoiceNumber;
    private UUID patientId;
    private String patientName;
    private UUID appointmentId;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal netAmount;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private LocalDateTime billingDate;
    private LocalDateTime dueDate;
    private String notes;
    private List<BillingItemResponseDTO> items;
    private LocalDateTime createdAt;

}

