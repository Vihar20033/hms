package com.hms.lab.service.impl;

import com.hms.appointment.entity.Appointment;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.common.audit.AuditLogService;
import com.hms.common.enums.LabOrderStatus;
import com.hms.common.enums.Role;
import com.hms.common.exception.BadRequestException;
import com.hms.common.util.SecurityUtils;
import com.hms.lab.dto.request.LabOrderRequest;
import com.hms.lab.dto.request.LabResultRequest;
import com.hms.lab.dto.response.LabOrderResponse;
import com.hms.lab.entity.LabOrder;
import com.hms.lab.repository.LabOrderRepository;
import com.hms.lab.service.LabOrderService;
import com.hms.patient.entity.Patient;
import com.hms.patient.exception.PatientNotFoundException;
import com.hms.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import com.hms.user.entity.User;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LabOrderServiceImpl implements LabOrderService {

    private final LabOrderRepository labOrderRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditLogService auditLogService;

    @Override
    public LabOrderResponse create(LabOrderRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));
        Appointment appointment = request.getAppointmentId() == null ? null : appointmentRepository.findById(request.getAppointmentId()).orElse(null);

        LabOrder order = LabOrder.builder()
                .patient(patient)
                .appointment(appointment)
                .testName(request.getTestName())
                .notes(request.getNotes())
                .status(LabOrderStatus.ORDERED)
                .build();

        LabOrder saved = labOrderRepository.save(order);
        auditLogService.log(SecurityUtils.getCurrentUsername(), "LAB_ORDER_CREATE", "LabOrder", saved.getId().toString(), "patientId=" + patient.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabOrderResponse> getAll(LabOrderStatus status) {
        List<LabOrder> orders = status == null
                ? labOrderRepository.findAll()
                : labOrderRepository.findByStatusOrderByCreatedAtAsc(status);
        return orders.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabOrderResponse> getByPatient(Long patientId) {
        User user = currentUser();
        if (user.getRole() == Role.PATIENT && !patientId.equals(resolveCurrentPatientId(user))) {
            throw new AccessDeniedException("You can only access your own lab reports.");
        }
        return labOrderRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabOrderResponse> getCurrentPatientOrders() {
        return getByPatient(resolveCurrentPatientId(currentUser()));
    }

    @Override
    public LabOrderResponse updateStatus(Long id, LabOrderStatus status) {
        LabOrder order = labOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lab order not found"));
        order.setStatus(status);
        LabOrder saved = labOrderRepository.save(order);
        auditLogService.log(SecurityUtils.getCurrentUsername(), "LAB_ORDER_STATUS", "LabOrder", id.toString(), "status=" + status);
        return toResponse(saved);
    }

    @Override
    public LabOrderResponse publishResult(Long id, LabResultRequest request) {
        LabOrder order = labOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lab order not found"));
        order.setResultSummary(request.getResultSummary());
        order.setNotes(request.getNotes());
        order.setStatus(LabOrderStatus.COMPLETED);
        LabOrder saved = labOrderRepository.save(order);
        auditLogService.log(SecurityUtils.getCurrentUsername(), "LAB_RESULT_PUBLISH", "LabOrder", id.toString(), "status=COMPLETED");
        return toResponse(saved);
    }

    private LabOrderResponse toResponse(LabOrder order) {
        return LabOrderResponse.builder()
                .id(order.getId())
                .patientId(order.getPatient().getId())
                .patientName(order.getPatient().getName())
                .appointmentId(order.getAppointment() == null ? null : order.getAppointment().getId())
                .testName(order.getTestName())
                .status(order.getStatus())
                .resultSummary(order.getResultSummary())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private Long resolveCurrentPatientId(User user) {
        return patientRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new BadRequestException("Your patient profile is not linked yet."))
                .getId();
    }
}
