package com.hms.pharmacy.dto.response;

import com.hms.common.enums.MedicineCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineResponseDTO {

    private String id;
    private String name;
    private String medicineCode;
    private String description;
    private MedicineCategory category;
    private String manufacturer;
    private LocalDate expiryDate;
    /** Fix #5 – Long to match entity type after overflow fix */
    private Long quantityInStock;
    private BigDecimal unitPrice;
    private Integer reorderLevel;
    /** Fix #8 – Dosage bounds field exposed to clients for UI warnings */
    private Integer maxSafeDose;
    private String dosage;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;
}
