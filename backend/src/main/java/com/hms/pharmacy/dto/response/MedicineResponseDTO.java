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
    private Integer quantityInStock;
    private BigDecimal unitPrice;
    private Integer reorderLevel;
    private String dosage;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;
}
