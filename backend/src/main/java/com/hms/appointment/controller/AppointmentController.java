package com.hms.appointment.controller;

import com.hms.common.response.ApiResponse;
import com.hms.appointment.dto.response.AppointmentResponseDTO;
import com.hms.appointment.mapper.AppointmentMapper;
import com.hms.common.enums.AppointmentStatus;
import com.hms.appointment.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import com.hms.common.enums.Department;
import jakarta.validation.Valid;
import com.hms.appointment.dto.request.AppointmentRequestDTO;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentMapper appointmentMapper;

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR','NURSE')")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<com.hms.appointment.dto.response.AppointmentSummaryDTO>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAppointmentSummary()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR')")
    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> create(@Valid @RequestBody AppointmentRequestDTO dto) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success(appointmentMapper.toDto(appointmentService.createAppointment(dto)), "Appointment scheduled successfully", org.springframework.http.HttpStatus.CREATED));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR','NURSE')")
    @GetMapping
    public ResponseEntity<ApiResponse<com.hms.common.response.PagedResponse<AppointmentResponseDTO>>> searchAppointments(
            @org.springframework.data.web.PageableDefault(size = 10, sort = "appointmentTime", direction = org.springframework.data.domain.Sort.Direction.DESC) org.springframework.data.domain.Pageable pageable,
            @RequestParam(name = "doctorId", required = false) UUID doctorId,
            @RequestParam(name = "patientId", required = false) UUID patientId,
            @RequestParam(name = "status", required = false) AppointmentStatus status,
            @RequestParam(name = "department", required = false) Department department,
            @RequestParam(name = "start", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime start,
            @RequestParam(name = "end", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime end,
            @RequestParam(name = "isEmergency", required = false) Boolean isEmergency) {
        
        org.springframework.data.domain.Page<com.hms.appointment.entity.Appointment> page = 
                appointmentService.findAppointments(pageable, doctorId, patientId, status, department, start, end, isEmergency);
        
        return ResponseEntity.ok(ApiResponse.success(
                com.hms.common.response.PagedResponse.from(page, appointmentMapper::toDto)));
    }

    /**
     * Get details of a single appointment. Service layer enforces ownership check.
     * Note: This is placed after static sub-paths (/my, /department, /patient) to avoid path collisions.
     */
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','RECEPTIONIST')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> getAppointmentById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(ApiResponse.success(appointmentMapper.toDto(appointmentService.getAppointmentById(id))));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> updateAppointment(
            @PathVariable("id") UUID id,
            @Valid @RequestBody AppointmentRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(appointmentMapper.toDto(appointmentService.updateAppointment(id, dto))));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> updateStatus(@PathVariable("id") UUID id,
            @RequestParam("status") AppointmentStatus status) {
        return ResponseEntity
                .ok(ApiResponse.success(appointmentMapper.toDto(appointmentService.updateStatus(id, status))));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAppointment(@PathVariable("id") UUID id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PreAuthorize("hasAnyRole('ADMIN','NURSE','RECEPTIONIST')")
    @PatchMapping("/{id}/check-in")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> checkInAppointment(@PathVariable("id") UUID id) {
        return ResponseEntity
                .ok(ApiResponse.success(appointmentMapper.toDto(appointmentService.updateStatus(id, AppointmentStatus.CHECKED_IN))));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @PatchMapping("/{id}/start")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> startConsultation(@PathVariable("id") UUID id) {
        return ResponseEntity
                .ok(ApiResponse.success(appointmentMapper.toDto(appointmentService.updateStatus(id, AppointmentStatus.IN_CONSULTATION))));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> completeConsultation(@PathVariable("id") UUID id) {
        return ResponseEntity
                .ok(ApiResponse.success(appointmentMapper.toDto(appointmentService.updateStatus(id, AppointmentStatus.COMPLETED))));
    }
}
