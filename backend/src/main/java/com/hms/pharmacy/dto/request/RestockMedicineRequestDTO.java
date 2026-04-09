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

    /** Fix #5 – Long qty; service also enforces Medicine.MAX_TRANSACTION_LIMIT (100,000) */
    @NotNull(message = "Quantity to add is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Long quantity;
   
}
