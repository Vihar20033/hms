package com.hms.appointment.service;

import com.hms.appointment.dto.request.AppointmentRequestDTO;
import com.hms.appointment.dto.response.AppointmentSummaryDTO;
import com.hms.appointment.entity.Appointment;
import com.hms.common.enums.AppointmentStatus;

import java.util.List;

public interface AppointmentService {

    AppointmentSummaryDTO getAppointmentSummary();

    Appointment createAppointment(AppointmentRequestDTO dto);

    Appointment getAppointmentById(Long id);

    Appointment updateAppointment(Long id, AppointmentRequestDTO dto);

    Appointment updateStatus(Long id, AppointmentStatus status);

    List<Appointment> getAppointments(Long patientId, AppointmentStatus status);

    List<Appointment> getTodayAppointments();

    void deleteAppointment(Long id);
}

