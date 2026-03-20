package com.hms.reporting.service;

import com.hms.reporting.dto.response.HospitalPerformanceDTO;
import com.hms.billing.repository.BillingRepository;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.patient.repository.PatientRepository;
import com.hms.appointment.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final BillingRepository billingRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public HospitalPerformanceDTO getHospitalPerformance() {
        // This is a simplified implementation for Phase 3
        BigDecimal totalRevenue = billingRepository.findAll().stream()
                .map(b -> b.getNetAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return HospitalPerformanceDTO.builder()
                .totalPatients(patientRepository.count())
                .totalDoctors(doctorRepository.count())
                .totalAppointments(appointmentRepository.count())
                .totalRevenue(totalRevenue)
                .departmentStats(new ArrayList<>()) // Placeholder
                .dailyRevenue(new ArrayList<>()) // Placeholder
                .build();
    }
}
