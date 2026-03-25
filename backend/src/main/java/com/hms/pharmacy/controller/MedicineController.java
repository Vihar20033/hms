package com.hms.pharmacy.controller;

import com.hms.common.response.ApiResponse;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/medicines")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MedicineController {

    private final MedicineService medicineService;

    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    @PostMapping
    public ResponseEntity<ApiResponse<MedicineResponseDTO>> createMedicine(
            @Valid @RequestBody MedicineRequestDTO dto) {
        MedicineResponseDTO created = medicineService.createMedicine(dto);
        ApiResponse<MedicineResponseDTO> response = ApiResponse.success(created);
        response.setMessage("Medicine created successfully");
        response.setStatus(HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicineResponseDTO>> updateMedicine(
            @PathVariable UUID id,
            @Valid @RequestBody MedicineRequestDTO dto) {
        MedicineResponseDTO updated = medicineService.updateMedicine(id, dto);
        ApiResponse<MedicineResponseDTO> response = ApiResponse.success(updated);
        response.setMessage("Medicine updated successfully");
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMedicine(@PathVariable UUID id) {
        medicineService.deleteMedicine(id);
        ApiResponse<Void> response = ApiResponse.success(null);
        response.setMessage("Medicine deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PHARMACIST')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicineResponseDTO>> getMedicineById(@PathVariable UUID id) {
        MedicineResponseDTO medicine = medicineService.getMedicineById(id);
        return ResponseEntity.ok(ApiResponse.success(medicine));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PHARMACIST')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<MedicineResponseDTO>>> getAllMedicines() {
        List<MedicineResponseDTO> medicines = medicineService.getAllMedicines();
        return ResponseEntity.ok(ApiResponse.success(medicines));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PHARMACIST')")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<MedicineResponseDTO>>> getActiveMedicines() {
        List<MedicineResponseDTO> medicines = medicineService.getActiveMedicines();
        return ResponseEntity.ok(ApiResponse.success(medicines));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PHARMACIST')")
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<MedicineResponseDTO>>> getMedicinesByCategory(
            @PathVariable String category) {
        List<MedicineResponseDTO> medicines = medicineService.getMedicinesByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(medicines));
    }


    @GetMapping("/check-code/{code}")
    public ResponseEntity<ApiResponse<Boolean>> checkMedicineCodeExists(@PathVariable String code) {
        boolean exists = medicineService.existsByMedicineCode(code);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    @PreAuthorize("hasRole('PHARMACIST')")
    @PostMapping("/dispense")
    public ResponseEntity<ApiResponse<Void>> dispenseMedicines(@Valid @RequestBody com.hms.pharmacy.dto.request.DispenseMedicineRequestDTO request) {
        medicineService.dispenseMedicines(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    @PatchMapping("/{id}/restock")
    public ResponseEntity<ApiResponse<Void>> restockMedicine(
            @PathVariable UUID id,
            @Valid @RequestBody RestockMedicineRequestDTO request) {
        medicineService.restockMedicine(id, request.getQuantity());
        ApiResponse<Void> response = ApiResponse.success(null);
        response.setMessage("Medicine restocked successfully");
        return ResponseEntity.ok(response);
    }

}

