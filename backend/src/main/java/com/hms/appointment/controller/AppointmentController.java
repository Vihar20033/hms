package com.hms.appointment.controller;

import com.hms.appointment.dto.response.AppointmentSummaryDTO;
import com.hms.appointment.entity.Appointment;
import com.hms.common.response.ApiResponse;
import com.hms.appointment.dto.response.AppointmentResponseDTO;
import com.hms.appointment.mapper.AppointmentMapper;
import com.hms.common.enums.AppointmentStatus;
import com.hms.appointment.service.AppointmentService;
import com.hms.common.response.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    public ResponseEntity<ApiResponse<AppointmentSummaryDTO>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(
                appointmentService.getAppointmentSummary()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR')")
    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> create(
            @Valid @RequestBody AppointmentRequestDTO dto) {
        return ResponseEntity.status(201).body(ApiResponse.success(
                        appointmentMapper.toDto(appointmentService.createAppointment(dto)), "Appointment scheduled successfully", org.springframework.http.HttpStatus.CREATED));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR','NURSE')")
    @GetMapping
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<PagedResponse<AppointmentResponseDTO>>> searchAppointments(
            @PageableDefault(sort = "appointmentTime", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(name = "doctorId", required = false) UUID doctorId,
            @RequestParam(name = "patientId", required = false) UUID patientId,
            @RequestParam(name = "status", required = false) AppointmentStatus status,
            @RequestParam(name = "department", required = false) Department department,
            @RequestParam(name = "start", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(name = "end", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(name = "isEmergency", required = false) Boolean isEmergency) {
        
        Page<Appointment> page = appointmentService.findAppointments(
                pageable, doctorId, patientId, status, department, start, end, isEmergency);
        
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.from(page, appointmentMapper::toDto)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','RECEPTIONIST')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> getAppointmentById(
            @PathVariable("id") UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                appointmentMapper.toDto(appointmentService.getAppointmentById(id))
        ));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> updateAppointment(
            @PathVariable("id") UUID id,
            @Valid @RequestBody AppointmentRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(
                appointmentMapper.toDto(appointmentService.updateAppointment(id, dto))));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> updateStatus(
            @PathVariable("id") UUID id,
            @RequestParam("status") AppointmentStatus status) {
        return ResponseEntity
                .ok(ApiResponse.success(
                        appointmentMapper.toDto(appointmentService.updateStatus(id, status))));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAppointment(
            @PathVariable("id") UUID id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PreAuthorize("hasAnyRole('ADMIN','NURSE','RECEPTIONIST')")
    @PatchMapping("/{id}/check-in")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> checkInAppointment(
            @PathVariable("id") UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                        appointmentMapper.toDto(appointmentService.updateStatus(id, AppointmentStatus.CHECKED_IN))));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @PatchMapping("/{id}/start")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> startConsultation(
            @PathVariable("id") UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                        appointmentMapper.toDto(appointmentService.updateStatus(id, AppointmentStatus.IN_CONSULTATION))));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> completeConsultation(
            @PathVariable("id") UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                        appointmentMapper.toDto(appointmentService.updateStatus(id, AppointmentStatus.COMPLETED))));
    }
}
