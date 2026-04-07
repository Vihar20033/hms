package com.hms.appointment.controller;

import com.hms.appointment.dto.response.AppointmentSummaryDTO;
import com.hms.common.response.ApiResponse;
import com.hms.appointment.dto.response.AppointmentResponseDTO;
import com.hms.appointment.mapper.AppointmentMapper;
import com.hms.common.enums.AppointmentStatus;
import com.hms.appointment.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import com.hms.appointment.dto.request.AppointmentRequestDTO;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentMapper appointmentMapper;

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR')")
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

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AppointmentResponseDTO>>> getAppointments(
            @RequestParam(name = "patientId", required = false) Long patientId,
            @RequestParam(name = "status", required = false) AppointmentStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                appointmentMapper.toDtoList(appointmentService.getAppointments(patientId, status))));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<AppointmentResponseDTO>>> getCurrentPatientAppointments(
            @RequestParam(name = "status", required = false) AppointmentStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                appointmentMapper.toDtoList(appointmentService.getCurrentPatientAppointments(status))));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR')")
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<AppointmentResponseDTO>>> getTodayAppointments() {
        return ResponseEntity.ok(ApiResponse.success(
                appointmentMapper.toDtoList(appointmentService.getTodayAppointments())));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST','PATIENT')")
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

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAppointment(
            @PathVariable("id") Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
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
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @PatchMapping("/reassign")
    public ResponseEntity<ApiResponse<Void>> reassignAppointments(
            @RequestParam("fromDoctorId") Long fromDoctorId,
            @RequestParam("toDoctorId") Long toDoctorId) {
        appointmentService.reassignAppointments(fromDoctorId, toDoctorId);
        return ResponseEntity.ok(ApiResponse.success(null, "Appointments reassigned successfully", org.springframework.http.HttpStatus.OK));
    }
}
