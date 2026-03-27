package com.hms.appointment.service;

import com.hms.appointment.dto.request.AppointmentRequestDTO;
import com.hms.appointment.dto.request.AppointmentSearchCriteria;
import com.hms.appointment.dto.response.AppointmentSummaryDTO;
import com.hms.appointment.entity.Appointment;
import com.hms.common.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppointmentService {

    AppointmentSummaryDTO getAppointmentSummary();

    Appointment createAppointment(AppointmentRequestDTO dto);

    Appointment getAppointmentById(Long id);

    Appointment updateAppointment(Long id, AppointmentRequestDTO dto);

    Appointment updateStatus(Long id, AppointmentStatus status);

    Page<Appointment> findAppointments(AppointmentSearchCriteria criteria, Pageable pageable);

    void deleteAppointment(Long id);
}

