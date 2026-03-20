package com.hms.laboratory.controller;

import com.hms.common.response.ApiResponse;
import com.hms.common.exception.BadRequestException;
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

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        log.info("=== LAB TEST REQUEST RECEIVED IN CONTROLLER ===");
        log.info("DTO: testName={}, testCode={}, price={}, patientId={}", 
                 dto != null ? dto.getTestName() : "null",
                 dto != null ? dto.getTestCode() : "null",
                 dto != null ? dto.getPrice() : "null",
                 dto != null ? dto.getPatientId() : "null");
        
        // Pre-validation: Ensure critical fields are not null
        if (dto == null) {
            log.error("Request body is null!");
            throw new BadRequestException("Request body cannot be empty");
        }
        if (dto.getPrice() == null) {
            log.error("Price is NULL in controller!");
            throw new BadRequestException("Lab test price is required and cannot be null");
        }
        if (dto.getTestName() == null || dto.getTestName().isEmpty()) {
            throw new BadRequestException("Lab test name is required");
        }
        if (dto.getTestCode() == null || dto.getTestCode().isEmpty()) {
            throw new BadRequestException("Lab test code is required");
        }
        if (dto.getPatientId() == null) {
            throw new BadRequestException("Patient ID is required");
        }
        
        log.info("All pre-validations passed. Calling service...");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(labService.requestTest(dto)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','LABORATORY_STAFF')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<LabTestResponseDTO>>> getAllTests() {
        return ResponseEntity.ok(ApiResponse.success(labService.getAllTests()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','LABORATORY_STAFF')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LabTestResponseDTO>> getTestById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(ApiResponse.success(labService.getTestById(id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','LABORATORY_STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LabTestResponseDTO>> updateTest(
            @PathVariable("id") UUID id, @Valid @RequestBody LabTestRequestDTO dto) {
        log.info("UPDATE TEST request for ID: {}", id);
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
        log.info("DELETE TEST request for ID: {} from user: {}", id, SecurityContextHolder.getContext().getAuthentication().getName());
        labService.deleteTest(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ====== Lab Report Endpoints ======

    @PreAuthorize("hasRole('LABORATORY_STAFF')")
    @PostMapping("/{testId}/report")
    public ResponseEntity<ApiResponse<LabReportResponseDTO>> createReport(
            @PathVariable("testId") UUID testId,
            @Valid @RequestBody LabReportRequestDTO dto) {
        log.info("Creating lab report for test ID: {}", testId);
        dto.setLabTestId(testId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(labService.createReport(dto)));
    }

    @PreAuthorize("hasRole('LABORATORY_STAFF')")
    @PutMapping("/report/{reportId}")
    public ResponseEntity<ApiResponse<LabReportResponseDTO>> updateReport(
            @PathVariable("reportId") UUID reportId,
            @Valid @RequestBody LabReportRequestDTO dto) {
        log.info("Updating lab report ID: {}", reportId);
        return ResponseEntity.ok(ApiResponse.success(labService.updateReport(reportId, dto)));
    }

    @PreAuthorize("hasAnyRole('LABORATORY_STAFF','ADMIN','DOCTOR')")
    @GetMapping("/{testId}/report")
    public ResponseEntity<ApiResponse<LabReportResponseDTO>> getReportByTestId(@PathVariable("testId") UUID testId) {
        return ResponseEntity.ok(ApiResponse.success(labService.getReportByTestId(testId)));
    }

    @PreAuthorize("hasAnyRole('LABORATORY_STAFF','ADMIN','DOCTOR')")
    @GetMapping("/report/{reportId}")
    public ResponseEntity<ApiResponse<LabReportResponseDTO>> getReportById(@PathVariable("reportId") UUID reportId) {
        return ResponseEntity.ok(ApiResponse.success(labService.getReportById(reportId)));
    }

    @PreAuthorize("hasAnyRole('LABORATORY_STAFF','ADMIN','DOCTOR')")
    @GetMapping("/reports/list")
    public ResponseEntity<ApiResponse<List<LabReportResponseDTO>>> getAllReports() {
        return ResponseEntity.ok(ApiResponse.success(labService.getAllReports()));
    }

    @DeleteMapping("/report/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN','LABORATORY_STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteReport(@PathVariable("reportId") UUID reportId) {
        log.info("DELETE REPORT request for ID: {} from user: {}", reportId, SecurityContextHolder.getContext().getAuthentication().getName());
        labService.deleteReport(reportId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    private void checkAdminRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            log.warn("Admin role check failed for user: {}. Authorities: {}", auth.getName(), auth.getAuthorities());
            throw new AccessDeniedException("Access Denied: Only Admin can delete lab records.");
        }
    }
}

