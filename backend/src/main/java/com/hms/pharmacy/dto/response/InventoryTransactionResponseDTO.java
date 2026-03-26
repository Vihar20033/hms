package com.hms.pharmacy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


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
    private Integer quantity;
    private Long referenceId;
    private String notes;
    private LocalDateTime createdAt;
    private String createdBy;
}
