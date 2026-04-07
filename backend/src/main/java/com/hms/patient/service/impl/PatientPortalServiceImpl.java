package com.hms.patient.service.impl;

import com.hms.appointment.mapper.AppointmentMapper;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.billing.service.BillingService;
import com.hms.lab.service.LabOrderService;
import com.hms.patient.dto.response.PatientPortalSummaryResponse;
import com.hms.patient.exception.PatientNotFoundException;
import com.hms.patient.mapper.PatientMapper;
import com.hms.patient.repository.PatientRepository;
import com.hms.patient.service.PatientPortalService;
import com.hms.prescription.service.PrescriptionService;
import com.hms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientPortalServiceImpl implements PatientPortalService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final PatientMapper patientMapper;
    private final LabOrderService labOrderService;
    private final BillingService billingService;
    private final PrescriptionService prescriptionService;

    @Override
    public PatientPortalSummaryResponse getCurrentPatientSummary() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new PatientNotFoundException("Patient user not found"));
        var patient = patientRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new PatientNotFoundException("Patient profile not found for current user"));

        return PatientPortalSummaryResponse.builder()
                .patient(patientMapper.toResponse(patient))
                .appointments(appointmentMapper.toDtoList(appointmentRepository.findByPatientIdOrderByAppointmentTimeDesc(patient.getId())))
                .labOrders(labOrderService.getByPatient(patient.getId()))
                .prescriptions(prescriptionService.getPrescriptionsByPatientId(patient.getId()))
                .billings(billingService.getCurrentPatientBillings())
                .build();
    }
}
