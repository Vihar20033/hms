package com.hms.pharmacy.dto.request;

import com.hms.common.validation.ValidationPatterns;
import com.hms.common.enums.MedicineCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineRequestDTO {

    @NotBlank(message = "Medicine name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @NotBlank(message = "Medicine code is required")
    @Size(max = 50, message = "Medicine code must not exceed 50 characters")
    @Pattern(regexp = ValidationPatterns.CODE, message = "Medicine code format is invalid")
    private String medicineCode;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Category is required")
    private MedicineCategory category;

    @Size(max = 100, message = "Manufacturer must not exceed 100 characters")
    @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9 .,&'()/-]{1,99}$", message = "Manufacturer contains invalid characters")
    private String manufacturer;

    @Size(max = 50, message = "Batch number must not exceed 50 characters")
    private String batchNumber;

    @Future(message = "Expiry date must be in the future")
    private LocalDate expiryDate;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be non-negative")
    private Integer quantityInStock;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be positive")
    @Digits(integer = 10, fraction = 2, message = "Unit price must have up to 2 decimal places")
    private BigDecimal unitPrice;

    @Min(value = 0, message = "Reorder level must be non-negative")
    private Integer reorderLevel;

    @Size(max = 100, message = "Storage location must not exceed 100 characters")
    private String storageLocation;

    @Size(max = 200, message = "Dosage must not exceed 200 characters")
    private String dosage;

    @Size(max = 500, message = "Side effects must not exceed 500 characters")
    private String sideEffects;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean requiresPrescription = false;
}
