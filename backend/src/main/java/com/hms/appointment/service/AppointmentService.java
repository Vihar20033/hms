package com.hms.appointment.service;

import com.hms.appointment.entity.Appointment;
import com.hms.common.enums.AppointmentStatus;

import java.util.List;
import java.util.UUID;

public interface AppointmentService {
    Appointment createAppointment(com.hms.appointment.dto.request.AppointmentRequestDTO dto);

    Appointment createAppointment(Appointment appointment);

    Appointment getAppointmentById(UUID id);

    Appointment updateAppointment(UUID id, com.hms.appointment.dto.request.AppointmentRequestDTO dto);

    Appointment updateStatus(UUID id, AppointmentStatus status);

    List<Appointment> getAppointmentsByDoctor(UUID doctorId);

    List<Appointment> getAppointmentsByDepartment(com.hms.common.enums.Department department);

    List<Appointment> getAppointmentsByPatient(UUID patientId);

    List<Appointment> getAllAppointments();

    void deleteAppointment(UUID id);
}

