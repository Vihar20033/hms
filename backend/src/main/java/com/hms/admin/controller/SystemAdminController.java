package com.hms.admin.controller;

import com.hms.admin.service.SystemAdminService;
import com.hms.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/system")
@PreAuthorize("hasRole('ADMIN')")
public class SystemAdminController {

    private final SystemAdminService systemAdminService;

    public SystemAdminController(SystemAdminService systemAdminService) {
        this.systemAdminService = systemAdminService;
    }

    @PostMapping("/restore/patient/{id}")
    public ResponseEntity<ApiResponse<Void>> restorePatient(@PathVariable Long id) {
        systemAdminService.restorePatient(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Patient restored successfully"));
    }

    @PostMapping("/restore/doctor/{id}")
    public ResponseEntity<ApiResponse<Void>> restoreDoctor(@PathVariable Long id) {
        systemAdminService.restoreDoctor(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Doctor restored successfully"));
    }

    @PostMapping("/restore/user/{id}")
    public ResponseEntity<ApiResponse<Void>> restoreUser(@PathVariable Long id) {
        systemAdminService.restoreUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User restored successfully"));
    }

    @PostMapping("/restore/billing/{id}")
    public ResponseEntity<ApiResponse<Void>> restoreBilling(@PathVariable Long id) {
        systemAdminService.restoreBilling(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Billing record restored successfully"));
    }
}