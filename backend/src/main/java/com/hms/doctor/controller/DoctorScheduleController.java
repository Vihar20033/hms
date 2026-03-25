package com.hms.doctor.controller;

import com.hms.common.response.ApiResponse;
import com.hms.doctor.dto.request.DoctorScheduleRequestDTO;
import com.hms.doctor.dto.response.DoctorScheduleResponseDTO;
import com.hms.doctor.service.DoctorScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctor-schedules")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DoctorScheduleController {

    private final DoctorScheduleService doctorScheduleService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<DoctorScheduleResponseDTO>> createSchedule(@Valid @RequestBody DoctorScheduleRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(doctorScheduleService.createSchedule(request), "Request successful", HttpStatus.CREATED));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ApiResponse<List<DoctorScheduleResponseDTO>>> getDoctorSchedules(@PathVariable("doctorId") UUID doctorId) {
        return ResponseEntity.ok(ApiResponse.success(doctorScheduleService.getDoctorSchedules(doctorId)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    @GetMapping("/doctor/{doctorId}/day/{dayOfWeek}")
    public ResponseEntity<ApiResponse<List<DoctorScheduleResponseDTO>>> getDoctorSchedulesByDay(
            @PathVariable("doctorId") UUID doctorId, @PathVariable("dayOfWeek") DayOfWeek dayOfWeek) {
        return ResponseEntity.ok(ApiResponse.success(doctorScheduleService.getDoctorSchedulesByDay(doctorId, dayOfWeek)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@PathVariable("id") UUID id) {
        doctorScheduleService.deleteSchedule(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
