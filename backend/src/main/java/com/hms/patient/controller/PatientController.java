package com.hms.patient.controller;

import com.hms.common.response.ApiResponse;
import com.hms.patient.dto.request.PatientRequestDTO;
import com.hms.patient.dto.response.PatientOnboardingResponseDTO;
import com.hms.patient.dto.response.PatientResponseDTO;
import com.hms.patient.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PatientController {

    private final PatientService service;

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @PostMapping
    public ApiResponse<PatientOnboardingResponseDTO> create(@Valid @RequestBody PatientRequestDTO dto) {
        return ApiResponse.success(service.create(dto));
    }

    /**
     * Search and list patients. Restricted to staff for hospital management.
     */
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','RECEPTIONIST','PHARMACIST','LABORATORY_STAFF')")
    @GetMapping
    public ApiResponse<Slice<PatientResponseDTO>> search(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "bloodGroup", required = false) String bloodGroup,
            @RequestParam(name = "urgencyLevel", required = false) String urgencyLevel,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy) {

        return ApiResponse.success(service.search(name, email, bloodGroup, urgencyLevel, page, size, sortBy));
    }

    /**
     * Listing all patients. Strictly restricted to clinical and administrative staff.
     */
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR','NURSE')")
    @GetMapping("/all")
    public ApiResponse<List<PatientResponseDTO>> getAll() {
        return ApiResponse.success(service.getAll());
    }

    /**
     * Securely fetch the logged-in patient's own profile metadata.
     */
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/my")
    public ApiResponse<PatientResponseDTO> getMyProfile() {
        return ApiResponse.success(service.getMyProfile());
    }

    /**
     * Fetch a specific patient profile. Service layer enforces identity/role checks.
     * Note: placed after static lookups to avoid path collisions.
     */
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','RECEPTIONIST','PHARMACIST','PATIENT')")
    @GetMapping("/{id}")
    public ApiResponse<PatientResponseDTO> getById(@PathVariable("id") UUID id) {
        return ApiResponse.success(service.getById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    @PutMapping("/{id}")
    public ApiResponse<PatientResponseDTO> update(@PathVariable("id") UUID id, @Valid @RequestBody PatientRequestDTO dto) {
        return ApiResponse.success(service.update(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','NURSE')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") UUID id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}
