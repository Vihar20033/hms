package com.hms.patient.controller;

import com.hms.common.response.ApiResponse;
import com.hms.patient.dto.response.PatientPortalSummaryResponse;
import com.hms.patient.service.PatientPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/patient-portal")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class PatientPortalController {

    private final PatientPortalService patientPortalService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<PatientPortalSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(patientPortalService.getCurrentPatientSummary()));
    }
}
