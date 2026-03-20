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
import lombok.RequiredArgsConstructor;
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
        @Transactional
        public Appointment createAppointment(com.hms.appointment.dto.request.AppointmentRequestDTO dto) {
                Appointment appointment = appointmentMapper.toEntity(dto);
                
                appointment.setPatient(patientRepository.findById(dto.getPatientId())
                                .orElseThrow(() -> new AppointmentNotFoundException("Patient not found with ID: " + dto.getPatientId())));
                
                if (dto.getDoctorId() != null) {
                        appointment.setDoctor(doctorRepository.findById(dto.getDoctorId())
                                        .orElseThrow(() -> new AppointmentNotFoundException("Doctor not found with ID: " + dto.getDoctorId())));
                } else {
                        List<com.hms.doctor.entity.Doctor> doctors = doctorRepository.findByDepartment(dto.getDepartment());
                        if (!doctors.isEmpty()) {
                                appointment.setDoctor(doctors.get(0));
                        } else {
                                throw new AppointmentNotFoundException("No doctors available in the " + dto.getDepartment() + " department.");
                        }
                }

                if (appointment.getDoctor() == null) {
                        throw new BadRequestException("A doctor must be selected for an appointment.");
                }

                appointment.setEmergency(dto.isEmergency());

                // Skip availability check for emergencies to "squeeze" them in
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
                // broadcastUpdate("APPOINTMENT_CREATE", saved);
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
                return appointmentRepository.findById(id)
                                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with ID: " + id));
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

                LocalDateTime requestedTime = LocalDateTime.of(dto.getAppointmentDate(), dto.getAppointmentTime());
                
                // Consistency: Use the same locking logic for updates
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
                if (isEmergency) {
                        return; // Emergency override: bypass all scheduling locks
                }
                
                // 1. Lock doctor slot to prevent two patients booking same doctor SAME time
                List<Appointment> doctorConflicts = appointmentRepository.findAndLockConflictingAppointments(
                        doctorId, time, ACTIVE_STATUSES);
                
                if (doctorConflicts.stream().anyMatch(a -> !a.getId().equals(currentAppointmentId))) {
                        throw new SlotAlreadyBookedException("The selected doctor is already booked for " + time);
                }

                // 2. Lock patient slot to prevent same patient booking two doctors SAME time
                List<Appointment> patientConflicts = appointmentRepository.findAndLockPatientConflictingAppointments(
                        patientId, time, ACTIVE_STATUSES);
                
                if (patientConflicts.stream().anyMatch(a -> !a.getId().equals(currentAppointmentId))) {
                        throw new SlotAlreadyBookedException("You already have an appointment scheduled for " + time);
                }
        }

        @Override
        @Transactional
        public Appointment updateStatus(UUID id, AppointmentStatus status) {
                Appointment appointment = appointmentRepository.findById(id)
                                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with ID: " + id));
                
                appointment.setStatus(status);
                Appointment saved = appointmentRepository.save(appointment);
                // broadcastUpdate("APPOINTMENT_STATUS_UPDATE", saved);
                auditLogService.log(getCurrentUsername(), "APPOINTMENT_STATUS_UPDATE", "Appointment", id.toString(), "status=" + status);
                return saved;
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
        @Transactional(readOnly = true)
        public List<Appointment> getAllAppointments() {
                return appointmentRepository.findAll();
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
                org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
        }
}

