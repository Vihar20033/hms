package com.hms.billing.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingItemRequestDTO {
    @NotBlank(message = "Item name is required")
    @Size(max = 200, message = "Item name must not exceed 200 characters")
    private String itemName;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be positive")
    @Digits(integer = 10, fraction = 2, message = "Unit price must have up to 2 decimal places")
    private BigDecimal unitPrice;

    @NotNull(message = "Total value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total value must be positive")
    @Digits(integer = 10, fraction = 2, message = "Total value must have up to 2 decimal places")
    private BigDecimal totalValue;
}
