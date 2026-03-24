package com.hms.appointment.service;

import com.hms.appointment.entity.Appointment;
import com.hms.common.enums.AppointmentStatus;

import java.util.List;
import java.util.UUID;

public interface AppointmentService {
    com.hms.appointment.dto.response.AppointmentSummaryDTO getAppointmentSummary();
    
    Appointment createAppointment(com.hms.appointment.dto.request.AppointmentRequestDTO dto);

    Appointment createAppointment(Appointment appointment);

    Appointment getAppointmentById(UUID id);

    Appointment updateAppointment(UUID id, com.hms.appointment.dto.request.AppointmentRequestDTO dto);

    Appointment updateStatus(UUID id, AppointmentStatus status);

    List<Appointment> getAppointmentsByDoctor(UUID doctorId);

    List<Appointment> getAppointmentsByDepartment(com.hms.common.enums.Department department);

    List<Appointment> getAppointmentsByPatient(UUID patientId);

    org.springframework.data.domain.Page<Appointment> findAppointments(
            org.springframework.data.domain.Pageable pageable,
            UUID doctorId,
            UUID patientId,
            com.hms.common.enums.AppointmentStatus status,
            com.hms.common.enums.Department department,
            java.time.LocalDateTime start,
            java.time.LocalDateTime end,
            Boolean isEmergency);

    void deleteAppointment(UUID id);
}

