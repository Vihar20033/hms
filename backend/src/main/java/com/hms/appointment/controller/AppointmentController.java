package com.hms.appointment.controller;

import org.springframework.transaction.annotation.Transactional;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import com.hms.appointment.dto.request.AppointmentRequestDTO;
import com.hms.appointment.dto.request.AppointmentSearchCriteria;

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
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<PagedResponse<AppointmentResponseDTO>>> searchAppointments(
            @ModelAttribute AppointmentSearchCriteria criteria,
            @PageableDefault(sort = "appointmentTime", direction = Sort.Direction.DESC, size = 20) Pageable pageable) {
        
        Page<Appointment> appointments = appointmentService.findAppointments(
                criteria, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.from(appointments, appointmentMapper::toDto)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','RECEPTIONIST')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> getAppointmentById(
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                appointmentMapper.toDto(appointmentService.getAppointmentById(id))
        ));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> updateAppointment(
            @PathVariable("id") Long id,
            @Valid @RequestBody AppointmentRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(
                appointmentMapper.toDto(appointmentService.updateAppointment(id, dto))));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") AppointmentStatus status) {
        return ResponseEntity
                .ok(ApiResponse.success(
                        appointmentMapper.toDto(appointmentService.updateStatus(id, status))));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAppointment(
            @PathVariable("id") Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PreAuthorize("hasAnyRole('ADMIN','NURSE','RECEPTIONIST')")
    @PatchMapping("/{id}/check-in")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> checkInAppointment(
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                        appointmentMapper.toDto(appointmentService.updateStatus(id, AppointmentStatus.CHECKED_IN))));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @PatchMapping("/{id}/start")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> startConsultation(
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                        appointmentMapper.toDto(appointmentService.updateStatus(id, AppointmentStatus.IN_CONSULTATION))));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> completeConsultation(
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                        appointmentMapper.toDto(appointmentService.updateStatus(id, AppointmentStatus.COMPLETED))));
    }
}
