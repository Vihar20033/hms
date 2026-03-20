package com.hms.pharmacy.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class DispenseMedicineRequestDTO {
    @NotNull(message = "Prescription ID is required")
    private UUID prescriptionId;
    
    @NotEmpty(message = "Items to dispense cannot be empty")
    @Valid
    private List<DispenseItemDTO> items;

    @Data
    public static class DispenseItemDTO {
        @NotNull(message = "Medicine ID is required")
        private UUID medicineId;
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }
}
