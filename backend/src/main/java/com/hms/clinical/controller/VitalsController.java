package com.hms.clinical.controller;

import com.hms.common.response.ApiResponse;
import com.hms.clinical.dto.request.VitalsRequestDTO;
import com.hms.clinical.dto.response.VitalsResponseDTO;
import com.hms.clinical.service.VitalsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vitals")
@RequiredArgsConstructor
public class VitalsController {

    private final VitalsService vitalsService;

    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE')")
    @PostMapping
    public ResponseEntity<ApiResponse<VitalsResponseDTO>> recordVitals(@Valid @RequestBody VitalsRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(vitalsService.recordVitals(dto)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ApiResponse<VitalsResponseDTO>> getVitalsByAppointment(@PathVariable UUID appointmentId) {
        return ResponseEntity.ok(ApiResponse.success(vitalsService.getVitalsByAppointment(appointmentId)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<java.util.List<VitalsResponseDTO>>> getVitalsToday() {
        // Simple implementation for now: fetch all
        return ResponseEntity.ok(ApiResponse.success(vitalsService.getAllVitals()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<java.util.List<VitalsResponseDTO>>> getVitalsByPatient(@PathVariable UUID patientId) {
        return ResponseEntity.ok(ApiResponse.success(vitalsService.getVitalsByPatientId(patientId)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VitalsResponseDTO>> updateVitals(@PathVariable UUID id, @Valid @RequestBody VitalsRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(vitalsService.updateVitals(id, dto)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVitals(@PathVariable UUID id) {
        vitalsService.deleteVitals(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
