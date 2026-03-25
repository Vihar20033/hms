package com.hms.pharmacy.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestockMedicineRequestDTO {
    @NotNull(message = "Quantity to add is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
