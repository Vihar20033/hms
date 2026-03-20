package com.hms.reporting.controller;

import com.hms.common.response.ApiResponse;
import com.hms.reporting.dto.response.HospitalPerformanceDTO;
import com.hms.reporting.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/performance")
    public ResponseEntity<ApiResponse<HospitalPerformanceDTO>> getHospitalPerformance() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getHospitalPerformance()));
    }
}
