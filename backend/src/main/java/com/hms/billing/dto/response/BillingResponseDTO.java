package com.hms.billing.dto.response;

import com.hms.common.enums.PaymentMethod;
import com.hms.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingResponseDTO {
    private Long id;
    private String invoiceNumber;
    private Long patientId;
    private String patientName;
    private Long appointmentId;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal netAmount;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private Instant billingDate;
    private Instant dueDate;
    private String notes;
    private String reportUrl;
    private List<BillingItemResponseDTO> items;
    private Instant createdAt;

}
