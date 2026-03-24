package com.hms.appointment.service;

import com.hms.appointment.dto.request.AppointmentRequestDTO;
import com.hms.appointment.dto.response.AppointmentSummaryDTO;
import com.hms.appointment.entity.Appointment;
import com.hms.common.enums.AppointmentStatus;
import com.hms.common.enums.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentService {

    AppointmentSummaryDTO getAppointmentSummary();
    
    Appointment createAppointment(AppointmentRequestDTO dto);

    Appointment createAppointment(Appointment appointment);

    Appointment getAppointmentById(UUID id);

    Appointment updateAppointment(UUID id, AppointmentRequestDTO dto);

    Appointment updateStatus(UUID id, AppointmentStatus status);

    Page<Appointment> findAppointments(
            Pageable pageable,
            UUID doctorId,
            UUID patientId,
            AppointmentStatus status,
            Department department,
            LocalDateTime start,
            LocalDateTime end,
            Boolean isEmergency);


    List<Appointment> getAppointmentsByDoctor(UUID doctorId);

    List<Appointment> getAppointmentsByDepartment(Department department);

    List<Appointment> getAppointmentsByPatient(UUID patientId);

    void deleteAppointment(UUID id);
}

