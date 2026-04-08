package com.hms.prescription.controller;

import com.hms.common.response.ApiResponse;
import com.hms.common.response.SliceResponse;
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

import java.util.List;


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
                .body(ApiResponse.success(
                        prescriptionService.createPrescription(dto), "Request successful", HttpStatus.CREATED));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST','DOCTOR')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PrescriptionResponseDTO>>> getAllPrescriptions() {
        return ResponseEntity.ok(
                ApiResponse.success(prescriptionService.getAllPrescriptions()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PHARMACIST')")
    @GetMapping("/slice")
    public ResponseEntity<ApiResponse<SliceResponse<PrescriptionResponseDTO>>> getPrescriptionSlice(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "15") int size,
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
    public ResponseEntity<ApiResponse<List<PrescriptionResponseDTO>>> getByPatientId(
            @PathVariable("patientId") Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(
                prescriptionService.getPrescriptionsByPatientId(patientId)));
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
