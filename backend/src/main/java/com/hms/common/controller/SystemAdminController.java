package com.hms.common.controller;

import com.hms.billing.repository.BillingRepository;
import com.hms.common.response.ApiResponse;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.patient.repository.PatientRepository;
import com.hms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/system")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SystemAdminController {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final BillingRepository billingRepository;

    @PostMapping("/restore/patient/{id}")
    public ResponseEntity<ApiResponse<Void>> restorePatient(@PathVariable Long id) {
        patientRepository.restore(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Patient restored successfully"));
    }

    @PostMapping("/restore/doctor/{id}")
    public ResponseEntity<ApiResponse<Void>> restoreDoctor(@PathVariable Long id) {
        doctorRepository.restore(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Doctor restored successfully"));
    }

    @PostMapping("/restore/user/{id}")
    public ResponseEntity<ApiResponse<Void>> restoreUser(@PathVariable Long id) {
        userRepository.restore(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User restored successfully"));
    }

    @PostMapping("/restore/billing/{id}")
    public ResponseEntity<ApiResponse<Void>> restoreBilling(@PathVariable Long id) {
        billingRepository.restore(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Billing record restored successfully"));
    }
}
