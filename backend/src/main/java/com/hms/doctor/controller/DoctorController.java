package com.hms.doctor.controller;

import com.hms.common.enums.Department;
import com.hms.common.response.ApiResponse;
import com.hms.common.response.SliceResponse;
import com.hms.doctor.dto.request.CreateDoctorRequest;
import com.hms.doctor.dto.request.UpdateDoctorRequest;
import com.hms.doctor.dto.response.DoctorOnboardingResponse;
import com.hms.doctor.dto.response.DoctorResponseDTO;
import com.hms.doctor.mapper.DoctorMapper;
import com.hms.doctor.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/{id}/appointment-count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getAppointmentCount(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(doctorService.getAppointmentCount(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DoctorResponseDTO>>> getAllDoctors() {
        return ResponseEntity.ok(ApiResponse.success
                (doctorMapper.toDtoList(doctorService.getAllDoctors())));
    }

    @GetMapping("/slice")
    public ResponseEntity<ApiResponse<SliceResponse<DoctorResponseDTO>>> getDoctorSlice(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "25") int size) {
        var slice = doctorService.getDoctorSlice(Math.max(page, 0), Math.min(Math.max(size, 1), 100))
                .map(doctorMapper::toDto);
        return ResponseEntity.ok(ApiResponse.success(SliceResponse.<DoctorResponseDTO>builder()
                .content(slice.getContent())
                .page(slice.getNumber())
                .size(slice.getSize())
                .first(slice.isFirst())
                .last(slice.isLast())
                .hasNext(slice.hasNext())
                .numberOfElements(slice.getNumberOfElements())
                .build()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorResponseDTO>> getDoctorById
            (@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success
                (doctorMapper.toDto(doctorService.getDoctorById(id))));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorOnboardingResponse>> createDoctor
            (@Valid @RequestBody CreateDoctorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success
                (doctorService.createDoctor(request), "Request successful", HttpStatus.CREATED));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponseDTO>> updateDoctor(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateDoctorRequest request) {
        return ResponseEntity.ok(ApiResponse.success
                (doctorMapper.toDto(doctorService.updateDoctor(id, request))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDoctor(@PathVariable("id") Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<ApiResponse<List<DoctorResponseDTO>>> getDoctorsByDepartment
            (@PathVariable("department") Department department) {
        return ResponseEntity.ok(ApiResponse.success
                (doctorMapper.toDtoList(doctorService.getDoctorsByDepartment(department))));
    }
}
