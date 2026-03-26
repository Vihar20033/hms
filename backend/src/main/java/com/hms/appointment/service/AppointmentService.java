package com.hms.appointment.service;

import com.hms.appointment.dto.request.AppointmentRequestDTO;
import com.hms.appointment.dto.response.AppointmentSummaryDTO;
import com.hms.appointment.entity.Appointment;
import com.hms.common.enums.AppointmentStatus;
import com.hms.common.enums.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import com.hms.appointment.dto.request.AppointmentSearchCriteria;


public interface AppointmentService {

    AppointmentSummaryDTO getAppointmentSummary();
    
    Appointment createAppointment(AppointmentRequestDTO dto);

    Appointment createAppointment(Appointment appointment);

    Appointment getAppointmentById(Long id);

    Appointment updateAppointment(Long id, AppointmentRequestDTO dto);

    Appointment updateStatus(Long id, AppointmentStatus status);

    Page<Appointment> findAppointments(AppointmentSearchCriteria criteria, Pageable pageable);


    List<Appointment> getAppointmentsByDoctor(Long doctorId);

    List<Appointment> getAppointmentsByDepartment(Department department);

    List<Appointment> getAppointmentsByPatient(Long patientId);

    void deleteAppointment(Long id);
}

