package com.hms.pharmacy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionResponseDTO {
    private UUID id;
    private UUID medicineId;
    private String medicineName;
    private String medicineCode;
    private String transactionType; // IN or OUT
    private Integer quantity;
    private UUID referenceId;
    private String notes;
    private LocalDateTime createdAt;
    private String createdBy;
}
