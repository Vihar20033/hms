package com.hms.patient.controller;

import com.hms.common.response.ApiResponse;
import com.hms.patient.dto.request.PatientRequestDTO;
import com.hms.patient.dto.request.PatientSearchCriteria;
import com.hms.patient.dto.response.PatientResponseDTO;
import com.hms.patient.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PatientController {

    private final PatientService service;

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @PostMapping
    public ApiResponse<PatientResponseDTO> create(
            @Valid @RequestBody PatientRequestDTO dto) {
        return ApiResponse.success(service.create(dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','RECEPTIONIST','PHARMACIST')")
    @GetMapping
    public ApiResponse<Slice<PatientResponseDTO>> search(
            @ModelAttribute PatientSearchCriteria criteria,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy) {

        return ApiResponse.success(service.search(criteria, page, size, sortBy));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR','NURSE')")
    @GetMapping("/all")
    public ApiResponse<List<PatientResponseDTO>> getAll() {
        return ApiResponse.success(service.getAll());
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','RECEPTIONIST','PHARMACIST')")
    @GetMapping("/{id}")
    public ApiResponse<PatientResponseDTO> getById(@PathVariable("id") Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    @PutMapping("/{id}")
    public ApiResponse<PatientResponseDTO> update(@PathVariable("id") Long id, @Valid @RequestBody PatientRequestDTO dto) {
        return ApiResponse.success(service.update(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','NURSE')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}
