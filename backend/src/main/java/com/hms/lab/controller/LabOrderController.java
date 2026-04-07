package com.hms.lab.controller;

import com.hms.common.enums.LabOrderStatus;
import com.hms.common.response.ApiResponse;
import com.hms.lab.dto.request.LabOrderRequest;
import com.hms.lab.dto.request.LabResultRequest;
import com.hms.lab.dto.response.LabOrderResponse;
import com.hms.lab.service.LabOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lab/orders")
@RequiredArgsConstructor
public class LabOrderController {

    private final LabOrderService labOrderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','LABORATORY_STAFF')")
    public ResponseEntity<ApiResponse<LabOrderResponse>> create(@Valid @RequestBody LabOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(labOrderService.create(request), "Lab order created", HttpStatus.CREATED));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','LABORATORY_STAFF')")
    public ResponseEntity<ApiResponse<List<LabOrderResponse>>> getAll(@RequestParam(required = false) LabOrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success(labOrderService.getAll(status)));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','LABORATORY_STAFF')")
    public ResponseEntity<ApiResponse<List<LabOrderResponse>>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(labOrderService.getByPatient(patientId)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<List<LabOrderResponse>>> getCurrentPatientOrders() {
        return ResponseEntity.ok(ApiResponse.success(labOrderService.getCurrentPatientOrders()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','LABORATORY_STAFF')")
    public ResponseEntity<ApiResponse<LabOrderResponse>> updateStatus(@PathVariable Long id, @RequestParam LabOrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success(labOrderService.updateStatus(id, status)));
    }

    @PatchMapping("/{id}/result")
    @PreAuthorize("hasAnyRole('ADMIN','LABORATORY_STAFF')")
    public ResponseEntity<ApiResponse<LabOrderResponse>> publishResult(@PathVariable Long id, @Valid @RequestBody LabResultRequest request) {
        return ResponseEntity.ok(ApiResponse.success(labOrderService.publishResult(id, request)));
    }
}
