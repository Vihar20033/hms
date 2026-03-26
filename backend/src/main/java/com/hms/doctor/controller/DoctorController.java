package com.hms.doctor.controller;

import com.hms.common.response.ApiResponse;
import com.hms.doctor.dto.request.CreateDoctorRequest;
import com.hms.doctor.dto.request.UpdateDoctorRequest;
import com.hms.doctor.dto.response.DoctorOnboardingResponse;
import com.hms.doctor.dto.response.DoctorResponseDTO;
import com.hms.doctor.entity.Doctor;
import com.hms.doctor.mapper.DoctorMapper;
import com.hms.doctor.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DoctorController {

    private final DoctorService doctorService;
    private final DoctorMapper doctorMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DoctorResponseDTO>>> getAllDoctors() {
        return ResponseEntity.ok(ApiResponse.success(doctorMapper.toDtoList(doctorService.getAllDoctors())));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<DoctorResponseDTO>>> searchDoctors(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) com.hms.common.enums.Department department,
            @RequestParam(required = false) Boolean isAvailable,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Doctor> doctors = doctorService.searchDoctors(query, department, isAvailable, pageable);
        return ResponseEntity.ok(ApiResponse.success(doctors.map(doctorMapper::toDto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorResponseDTO>> getDoctorById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(doctorMapper.toDto(doctorService.getDoctorById(id))));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorOnboardingResponse>> createDoctor(@Valid @RequestBody CreateDoctorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(doctorService.createDoctor(request), "Request successful", HttpStatus.CREATED));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponseDTO>> updateDoctor(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateDoctorRequest request) {
        return ResponseEntity.ok(ApiResponse.success(doctorMapper.toDto(doctorService.updateDoctor(id, request))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDoctor(@PathVariable("id") Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<DoctorResponseDTO>> getMyProfile(@RequestParam("userId") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(doctorMapper.toDto(doctorService.getDoctorByUserId(userId))));
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<ApiResponse<List<DoctorResponseDTO>>> getDoctorsByDepartment(@PathVariable("department") com.hms.common.enums.Department department) {
        return ResponseEntity.ok(ApiResponse.success(doctorMapper.toDtoList(doctorService.getDoctorsByDepartment(department))));
    }
}
