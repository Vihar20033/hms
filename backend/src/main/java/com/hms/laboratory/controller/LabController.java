package com.hms.laboratory.controller;

import com.hms.common.response.ApiResponse;
import com.hms.laboratory.dto.request.LabReportRequestDTO;
import com.hms.laboratory.dto.request.LabTestRequestDTO;
import com.hms.laboratory.dto.response.LabReportResponseDTO;
import com.hms.laboratory.dto.response.LabTestResponseDTO;
import com.hms.common.enums.TestStatus;
import com.hms.laboratory.service.LabService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lab-tests")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class LabController {

    private final LabService labService;

    @PreAuthorize("hasAnyRole('DOCTOR','LABORATORY_STAFF','ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<LabTestResponseDTO>> requestTest(@Valid @RequestBody LabTestRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(labService.requestTest(dto)));
    }

    /**
     * Master test list. Management/Lab staff only.
     */
    @PreAuthorize("hasAnyRole('ADMIN','LABORATORY_STAFF','DOCTOR')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<LabTestResponseDTO>>> getAllTests() {
        return ResponseEntity.ok(ApiResponse.success(labService.getAllTests()));
    }


    /**
     * Get details of a single report. Note: placed after static lookups to avoid path collisions.
     */
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','LABORATORY_STAFF')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LabTestResponseDTO>> getTestById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(ApiResponse.success(labService.getTestById(id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','LABORATORY_STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LabTestResponseDTO>> updateTest(
            @PathVariable("id") UUID id, @Valid @RequestBody LabTestRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(labService.updateTest(id, dto)));
    }

    @PreAuthorize("hasRole('LABORATORY_STAFF')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<LabTestResponseDTO>> updateStatus(
            @PathVariable("id") UUID id, @RequestParam("status") TestStatus status) {
        return ResponseEntity.ok(ApiResponse.success(labService.updateTestStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LABORATORY_STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteTest(@PathVariable("id") UUID id) {
        labService.deleteTest(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ====== Lab Report Endpoints ======

    @PreAuthorize("hasRole('LABORATORY_STAFF')")
    @PostMapping("/{testId}/report")
    public ResponseEntity<ApiResponse<LabReportResponseDTO>> createReport(
            @PathVariable("testId") UUID testId,
            @Valid @RequestBody LabReportRequestDTO dto) {
        dto.setLabTestId(testId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(labService.createReport(dto)));
    }


    @PreAuthorize("hasAnyRole('LABORATORY_STAFF','ADMIN','DOCTOR')")
    @GetMapping("/{testId}/report")
    public ResponseEntity<ApiResponse<LabReportResponseDTO>> getReportByTestId(@PathVariable("testId") UUID testId) {
        return ResponseEntity.ok(ApiResponse.success(labService.getReportByTestId(testId)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','LABORATORY_STAFF')")
    @GetMapping("/reports/list")
    public ResponseEntity<ApiResponse<List<LabReportResponseDTO>>> getAllReports() {
        return ResponseEntity.ok(ApiResponse.success(labService.getAllReports()));
    }

    @DeleteMapping("/report/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN','LABORATORY_STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteReport(@PathVariable("reportId") UUID reportId) {
        labService.deleteReport(reportId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
