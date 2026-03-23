package com.hms.prescription.controller;

import com.hms.common.response.ApiResponse;
import com.hms.prescription.dto.request.PrescriptionRequestDTO;
import com.hms.prescription.dto.response.PrescriptionResponseDTO;
import com.hms.prescription.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @PostMapping
    public ResponseEntity<ApiResponse<PrescriptionResponseDTO>> createPrescription(
            @Valid @RequestBody PrescriptionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(prescriptionService.createPrescription(dto), "Request successful", HttpStatus.CREATED));
    }

    /**
     * Fetch all prescriptions. Management/Pharmacist role only.
     */
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST','DOCTOR')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PrescriptionResponseDTO>>> getAllPrescriptions() {
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.getAllPrescriptions()));
    }


    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PHARMACIST')")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<PrescriptionResponseDTO>>> getByPatientId(@PathVariable UUID patientId) {
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.getPrescriptionsByPatientId(patientId)));
    }

    /**
     * Fetch specific prescription by ID. Service layer enforces identity checks.
     * Note: placed after static lookups to avoid path collisions.
     */
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PHARMACIST')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PrescriptionResponseDTO>> getPrescriptionById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.getPrescriptionById(id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePrescription(@PathVariable UUID id) {
        prescriptionService.deletePrescription(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
