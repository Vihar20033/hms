package com.hms.prescription.controller;

import com.hms.common.response.ApiResponse;
import com.hms.common.response.SliceResponse;
import com.hms.security.idempotency.IdempotencyService;
import com.hms.security.util.SecurityUtils;
import com.hms.prescription.dto.request.PrescriptionRequestDTO;
import com.hms.prescription.dto.response.PrescriptionResponseDTO;
import com.hms.prescription.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Slice;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
        private final IdempotencyService idempotencyService;

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @PostMapping
    public ResponseEntity<ApiResponse<PrescriptionResponseDTO>> createPrescription(
            @Valid @RequestBody PrescriptionRequestDTO dto,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey) {
        String currentUser = SecurityUtils.getCurrentUsername();
        String fingerprint = idempotencyService.computeFingerprint(dto);

        PrescriptionResponseDTO response = idempotencyService.execute(
                idempotencyKey,
                "prescription-create",
                currentUser,
                fingerprint,
                () -> prescriptionService.createPrescription(dto),
                PrescriptionResponseDTO::getId,
                prescriptionService::getPrescriptionById);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        response, "Request successful", HttpStatus.CREATED));
    }

    // Fix #10 - Empty Search Performance: Removed generic / endpoint. 
    // Clients must use /slice with pagination to prevent server memory issues.

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PHARMACIST')")
    @GetMapping("/slice")
    public ResponseEntity<ApiResponse<SliceResponse<PrescriptionResponseDTO>>> getPrescriptionSlice(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query) {
        Slice<PrescriptionResponseDTO> slice = prescriptionService.getPrescriptionSlice(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                query);

        return ResponseEntity.ok(ApiResponse.success(SliceResponse.<PrescriptionResponseDTO>builder()
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
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<SliceResponse<PrescriptionResponseDTO>>> getByPatientId(
            @PathVariable("patientId") Long patientId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "15") int size) {
        
        // Fix #10 - Enforce mandatory pagination 
        org.springframework.data.domain.Slice<PrescriptionResponseDTO> slice = prescriptionService.getPrescriptionSliceByPatient(
                patientId, Math.max(page, 0), Math.min(Math.max(size, 1), 100));

        return ResponseEntity.ok(ApiResponse.success(SliceResponse.<PrescriptionResponseDTO>builder()
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
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PrescriptionResponseDTO>> getPrescriptionById(
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                prescriptionService.getPrescriptionById(id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePrescription(
            @PathVariable("id") Long id) {
        prescriptionService.deletePrescription(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
