package com.hms.pharmacy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionResponseDTO {
    private Long id;
    private Long medicineId;
    private String medicineName;
    private String medicineCode;
    private String transactionType; // IN or OUT
    /** Fix #5 – Long quantity to match entity after overflow fix */
    private Long quantity;
    private Long referenceId;
    private String notes;
    private Instant createdAt;
    private String createdBy;
}
