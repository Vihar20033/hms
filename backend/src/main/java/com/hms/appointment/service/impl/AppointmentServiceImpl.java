package com.hms.appointment.service.impl;

import com.hms.appointment.entity.Appointment;
import com.hms.appointment.exception.AppointmentNotFoundException;
import com.hms.appointment.exception.SlotAlreadyBookedException;
import com.hms.appointment.mapper.AppointmentMapper;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.appointment.service.AppointmentService;
import com.hms.common.enums.AppointmentStatus;
import com.hms.common.exception.BadRequestException;
import com.hms.doctor.entity.Doctor;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.patient.entity.Patient;
import com.hms.patient.repository.PatientRepository;
import com.hms.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private static final List<AppointmentStatus> ACTIVE_STATUSES = Arrays.asList(
            AppointmentStatus.SCHEDULED,
            AppointmentStatus.CHECKED_IN,
            AppointmentStatus.IN_CONSULTATION
    );

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentMapper appointmentMapper;
    private final com.hms.common.audit.AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public com.hms.appointment.dto.response.AppointmentSummaryDTO getAppointmentSummary() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        com.hms.appointment.dto.response.AppointmentSummaryDTO.AppointmentSummaryDTOBuilder builder = 
                com.hms.appointment.dto.response.AppointmentSummaryDTO.builder();

        if (user.getRole() == com.hms.common.enums.Role.DOCTOR) {
            Doctor doctor = doctorRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BadRequestException("Current user is not registered as a doctor."));
            UUID doctorId = doctor.getId();

            builder.total(appointmentRepository.countByDoctorId(doctorId))
                   .scheduled(appointmentRepository.countByDoctorIdAndStatus(doctorId, AppointmentStatus.SCHEDULED))
                   .checkedIn(appointmentRepository.countByDoctorIdAndStatus(doctorId, AppointmentStatus.CHECKED_IN))
                   .inConsultation(appointmentRepository.countByDoctorIdAndStatus(doctorId, AppointmentStatus.IN_CONSULTATION))
                   .completed(appointmentRepository.countByDoctorIdAndStatus(doctorId, AppointmentStatus.COMPLETED))
                   .cancelled(appointmentRepository.countByDoctorIdAndStatus(doctorId, AppointmentStatus.CANCELLED));
        } else {
            builder.total(appointmentRepository.count())
                   .scheduled(appointmentRepository.countByStatus(AppointmentStatus.SCHEDULED))
                   .checkedIn(appointmentRepository.countByStatus(AppointmentStatus.CHECKED_IN))
                   .inConsultation(appointmentRepository.countByStatus(AppointmentStatus.IN_CONSULTATION))
                   .completed(appointmentRepository.countByStatus(AppointmentStatus.COMPLETED))
                   .cancelled(appointmentRepository.countByStatus(AppointmentStatus.CANCELLED));
        }

        return builder.build();
    }

    @Override
    @Transactional
    public Appointment createAppointment(com.hms.appointment.dto.request.AppointmentRequestDTO dto) {
        Appointment appointment = appointmentMapper.toEntity(dto);
        
        appointment.setPatient(patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new AppointmentNotFoundException("Patient not found with ID: " + dto.getPatientId())));
        
        Doctor doctor;
        if (dto.getDoctorId() != null) {
            doctor = doctorRepository.findById(dto.getDoctorId())
                    .orElseThrow(() -> new AppointmentNotFoundException("Doctor not found with ID: " + dto.getDoctorId()));
        } else {
            List<Doctor> doctors = doctorRepository.findByDepartment(dto.getDepartment());
            if (!doctors.isEmpty()) {
                doctor = doctors.get(0);
            } else {
                throw new AppointmentNotFoundException("No doctors available in the " + dto.getDepartment() + " department.");
            }
        }
        
        // 🔒 Integrity Check: Ensure doctor belongs to the department
        if (doctor.getDepartment() != dto.getDepartment()) {
            throw new BadRequestException("Doctor " + doctor.getFirstName() + " does not belong to the " + dto.getDepartment() + " department.");
        }
        
        appointment.setDoctor(doctor);
        appointment.setEmergency(dto.isEmergency());

        lockAndCheckAvailability(appointment.getDoctor().getId(), appointment.getPatient().getId(), 
                appointment.getAppointmentTime(), appointment.getId(), appointment.isEmergency());

        LocalDateTime requestedTime = appointment.getAppointmentTime();
        LocalDateTime startOfDay = requestedTime.with(LocalTime.MIN);
        LocalDateTime endOfDay = requestedTime.with(LocalTime.MAX);
        long todayCount = appointmentRepository.countByDoctorIdAndAppointmentTimeBetween(
                appointment.getDoctor().getId(), startOfDay, endOfDay);
        
        String prefix = appointment.isEmergency() ? "EM" : "P";
        appointment.setTokenNumber(String.format("%s-%03d", prefix, todayCount + 1));
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        
        Appointment saved = appointmentRepository.save(appointment);
        auditLogService.log(getCurrentUsername(), "APPOINTMENT_CREATE", "Appointment", saved.getId().toString(), "patient=" + saved.getPatient().getName());
        return saved;
    }

    @Override
    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        if (appointment.getStatus() == null) {
            appointment.setStatus(AppointmentStatus.SCHEDULED);
        }

        if (appointment.getDoctor() != null && appointment.getPatient() != null && appointment.getAppointmentTime() != null) {
            lockAndCheckAvailability(appointment.getDoctor().getId(), appointment.getPatient().getId(), 
                    appointment.getAppointmentTime(), appointment.getId(), appointment.isEmergency());

            if (appointment.getTokenNumber() == null) {
                LocalDateTime requestedTime = appointment.getAppointmentTime();
                LocalDateTime startOfDay = requestedTime.with(LocalTime.MIN);
                LocalDateTime endOfDay = requestedTime.with(LocalTime.MAX);
                long todayCount = appointmentRepository.countByDoctorIdAndAppointmentTimeBetween(
                                appointment.getDoctor().getId(), startOfDay, endOfDay);
                appointment.setTokenNumber(String.format("P-%03d", todayCount + 1));
            }
        }

        Appointment saved = appointmentRepository.save(appointment);
        auditLogService.log(getCurrentUsername(), "APPOINTMENT_CREATE_RAW", "Appointment", saved.getId().toString(), "status=" + saved.getStatus());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Appointment getAppointmentById(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with ID: " + id));
        
        // 🔒 Security Check
        checkOwnership(appointment);
        
        return appointment;
    }

    @Override
    @Transactional
    public Appointment updateAppointment(UUID id, com.hms.appointment.dto.request.AppointmentRequestDTO dto) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with ID: " + id));

        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new AppointmentNotFoundException("Patient not found with ID: " + dto.getPatientId()));

        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new AppointmentNotFoundException("Doctor not found with ID: " + dto.getDoctorId()));

        // 🔒 Integrity Check
        if (doctor.getDepartment() != dto.getDepartment()) {
            throw new BadRequestException("Doctor " + doctor.getFirstName() + " does not belong to the " + dto.getDepartment() + " department.");
        }

        LocalDateTime requestedTime = LocalDateTime.of(dto.getAppointmentDate(), dto.getAppointmentTime());
        lockAndCheckAvailability(doctor.getId(), patient.getId(), requestedTime, id, dto.isEmergency());

        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setDepartment(dto.getDepartment());
        appointment.setAppointmentTime(requestedTime);
        appointment.setReason(dto.getReason());
        appointment.setNotes(dto.getNotes());
        appointment.setEmergency(dto.isEmergency());

        Appointment saved = appointmentRepository.save(appointment);
        auditLogService.log(getCurrentUsername(), "APPOINTMENT_UPDATE", "Appointment", id.toString(), "patient=" + saved.getPatient().getName());
        return saved;
    }

    private void lockAndCheckAvailability(UUID doctorId, UUID patientId, LocalDateTime time, UUID currentAppointmentId, boolean isEmergency) {
        if (isEmergency) return;
        
        List<Appointment> doctorConflicts = appointmentRepository.findAndLockConflictingAppointments(doctorId, time, ACTIVE_STATUSES);
        if (doctorConflicts.stream().anyMatch(a -> !a.getId().equals(currentAppointmentId))) {
            throw new SlotAlreadyBookedException("Doctor is already booked for " + time);
        }

        List<Appointment> patientConflicts = appointmentRepository.findAndLockPatientConflictingAppointments(patientId, time, ACTIVE_STATUSES);
        if (patientConflicts.stream().anyMatch(a -> !a.getId().equals(currentAppointmentId))) {
            throw new SlotAlreadyBookedException("Patient already has an appointment for " + time);
        }
    }

    @Override
    @Transactional
    public Appointment updateStatus(UUID id, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with ID: " + id));
        
        // 🔒 Security Check: Only assigned doctor can update status to consultation/completed
        if (status == AppointmentStatus.IN_CONSULTATION || status == AppointmentStatus.COMPLETED) {
            checkOwnership(appointment);
        }

        appointment.setStatus(status);
        Appointment saved = appointmentRepository.save(appointment);
        auditLogService.log(getCurrentUsername(), "APPOINTMENT_STATUS_UPDATE", "Appointment", id.toString(), "status=" + status);
        return saved;
    }

    /**
     * Verifies that the current user has authority to access or modify this specific appointment.
     * Authorized roles: ADMIN, RECEPTIONIST, NURSE.
     * Specific access: Assigned DOCTOR, related PATIENT.
     */
    private void checkOwnership(Appointment appointment) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        com.hms.common.enums.Role role = user.getRole();

        // 1. Staff with Global Access (Management & Triage)
        if (role == com.hms.common.enums.Role.ADMIN || 
            role == com.hms.common.enums.Role.RECEPTIONIST || 
            role == com.hms.common.enums.Role.NURSE) {
            return;
        }

        // 2. Doctor specific access
        if (role == com.hms.common.enums.Role.DOCTOR) {
            if (appointment.getDoctor() != null && appointment.getDoctor().getUserId().equals(user.getId())) {
                return;
            }
        }


        log.warn("Security Alert: User {} with role {} attempted unauthorized access/update for appointment {}.", 
                user.getUsername(), role, appointment.getId());
        throw new AccessDeniedException("You do not have permission to access this appointment.");
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Appointment> findAppointments(
            org.springframework.data.domain.Pageable pageable,
            UUID doctorId,
            UUID patientId,
            AppointmentStatus status,
            com.hms.common.enums.Department department,
            LocalDateTime start,
            LocalDateTime end,
            Boolean isEmergency) {
        
        org.springframework.data.jpa.domain.Specification<Appointment> spec = org.springframework.data.jpa.domain.Specification.where(null);
        
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getRole() == com.hms.common.enums.Role.DOCTOR) {
            spec = spec.and(com.hms.appointment.specification.AppointmentSpecification.hasDoctorUserId(user.getId()));
        } else if (user.getRole() == com.hms.common.enums.Role.PATIENT) {
             // Future: filter for patient's own record
        }

        if (doctorId != null) spec = spec.and(com.hms.appointment.specification.AppointmentSpecification.hasDoctorId(doctorId));
        if (patientId != null) spec = spec.and(com.hms.appointment.specification.AppointmentSpecification.hasPatientId(patientId));
        if (status != null) spec = spec.and(com.hms.appointment.specification.AppointmentSpecification.hasStatus(status));
        if (department != null) spec = spec.and(com.hms.appointment.specification.AppointmentSpecification.hasDepartment(department));
        if (start != null || end != null) spec = spec.and(com.hms.appointment.specification.AppointmentSpecification.hasTimeBetween(start, end));
        if (isEmergency != null) spec = spec.and(com.hms.appointment.specification.AppointmentSpecification.isEmergency(isEmergency));

        return appointmentRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByDoctor(UUID doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByDepartment(com.hms.common.enums.Department department) {
        return appointmentRepository.findByDepartment(department);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByPatient(UUID patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    @Override
    @Transactional
    public void deleteAppointment(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with ID: " + id));
        appointment.setDeleted(true);
        appointmentRepository.save(appointment);
        auditLogService.log(getCurrentUsername(), "APPOINTMENT_DELETE", "Appointment", id.toString(), "deleted=true");
    }

    private String getCurrentUsername() {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }
}
