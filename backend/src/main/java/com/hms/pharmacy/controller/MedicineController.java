package com.hms.pharmacy.controller;

import com.hms.common.response.ApiResponse;
import com.hms.common.response.SliceResponse;
import com.hms.pharmacy.dto.request.DispenseMedicineRequestDTO;
import com.hms.pharmacy.dto.request.MedicineRequestDTO;
import com.hms.pharmacy.dto.request.RestockMedicineRequestDTO;
import com.hms.pharmacy.dto.response.MedicineResponseDTO;
import com.hms.pharmacy.service.MedicineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicines")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MedicineController {

    private final MedicineService service;

    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    @PostMapping
    public ResponseEntity<ApiResponse<MedicineResponseDTO>> create(
            @Valid @RequestBody MedicineRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createMedicine(dto), "Request successful", HttpStatus.CREATED));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicineResponseDTO>> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody MedicineRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(service.updateMedicine(id, dto)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        service.deleteMedicine(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PHARMACIST')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicineResponseDTO>> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getMedicineById(id)));
    }

    @PreAuthorize("hasAnyRole('PHARMACIST', 'DOCTOR', 'ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<MedicineResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllMedicines()));
    }

    @PreAuthorize("hasAnyRole('PHARMACIST', 'DOCTOR', 'ADMIN')")
    @GetMapping("/slice")
    public ResponseEntity<ApiResponse<SliceResponse<MedicineResponseDTO>>> getSlice(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "25") int size,
            @RequestParam(name = "query", required = false) String query) {
        
        org.springframework.data.domain.Slice<MedicineResponseDTO> slice;
        if (query != null && !query.isEmpty()) {
            slice = service.getSearchableMedicineSlice(Math.max(page, 0), Math.min(Math.max(size, 1), 100), query);
        } else {
            slice = service.getMedicineSlice(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        }

        return ResponseEntity.ok(ApiResponse.success(SliceResponse.<MedicineResponseDTO>builder()
                .content(slice.getContent())
                .page(slice.getNumber())
                .size(slice.getSize())
                .first(slice.isFirst())
                .last(slice.isLast())
                .hasNext(slice.hasNext())
                .numberOfElements(slice.getNumberOfElements())
                .build()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PHARMACIST')")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<MedicineResponseDTO>>> getActive() {
        return ResponseEntity.ok(ApiResponse.success(service.getActiveMedicines()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PHARMACIST')")
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<MedicineResponseDTO>>> getByCategory(
            @PathVariable("category") String category) {
        return ResponseEntity.ok(ApiResponse.success(service.getMedicinesByCategory(category)));
    }

    @GetMapping("/check-code/{code}")
    public ResponseEntity<ApiResponse<Boolean>> checkCode(@PathVariable("code") String code) {
        return ResponseEntity.ok(ApiResponse.success(service.existsByMedicineCode(code)));
    }

    @PreAuthorize("hasRole('PHARMACIST')")
    @PostMapping("/dispense")
    public ResponseEntity<ApiResponse<Void>> dispense(
            @Valid @RequestBody DispenseMedicineRequestDTO request) {
        service.dispenseMedicines(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    @PatchMapping("/{id}/restock")
    public ResponseEntity<ApiResponse<Void>> restock(
            @PathVariable("id") Long id,
            @Valid @RequestBody RestockMedicineRequestDTO request) {
        service.restockMedicine(id, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
