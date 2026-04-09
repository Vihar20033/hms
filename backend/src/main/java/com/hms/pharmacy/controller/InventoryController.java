package com.hms.pharmacy.controller;

import com.hms.common.response.ApiResponse;
import com.hms.common.response.SliceResponse;
import com.hms.pharmacy.dto.response.InventoryTransactionResponseDTO;
import com.hms.pharmacy.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pharmacy/inventory-log")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
public class InventoryController {

    private final MedicineService medicineService;

    @GetMapping
    public ResponseEntity<ApiResponse<SliceResponse<InventoryTransactionResponseDTO>>> getTransactions(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        // Fix #10 - Enforce Mandatory Pagination 
        Slice<InventoryTransactionResponseDTO> slice = medicineService.getTransactionSlice(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100));

        return ResponseEntity.ok(ApiResponse.success(SliceResponse.<InventoryTransactionResponseDTO>builder()
                .content(slice.getContent())
                .page(slice.getNumber())
                .size(slice.getSize())
                .first(slice.isFirst())
                .last(slice.isLast())
                .hasNext(slice.hasNext())
                .numberOfElements(slice.getNumberOfElements())
                .build()));
    }
}
