package com.hms.patient.service.impl;

import com.hms.common.audit.AuditLogService;
import com.hms.common.util.SecurityUtils;
import com.hms.patient.dto.request.PatientRequestDTO;
import com.hms.patient.dto.response.PatientResponseDTO;
import com.hms.patient.entity.Patient;
import com.hms.patient.exception.DuplicatePatientException;
import com.hms.patient.exception.PatientNotFoundException;
import com.hms.patient.mapper.PatientMapper;
import com.hms.patient.repository.PatientRepository;
import com.hms.patient.service.PatientService;
import com.hms.user.repository.UserRepository;
import com.hms.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hms.common.enums.Role;
import com.hms.user.entity.User;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import com.hms.common.enums.AppointmentStatus;
import com.hms.common.exception.BadRequestException;
import com.hms.appointment.repository.AppointmentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository repository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final AuditLogService auditLogService;
    private final PatientMapper mapper;
    private final AppointmentRepository appointmentRepository;

    @Override
    @CacheEvict(value = "patients", allEntries = true)
    public PatientResponseDTO create(PatientRequestDTO dto) {
        log.info("Creating patient with contact number: {}", dto.getContactNumber());
        if (repository.existsByContactNumber(dto.getContactNumber())) {
            throw new DuplicatePatientException("Patient already exists");
        }

        Patient saved = repository.save(mapper.toEntity(dto));
        log.info("Patient created successfully with ID: {}", saved.getId());
        auditLogService.log(SecurityUtils.getCurrentUsername(), "PATIENT_CREATE", "Patient", saved.getId().toString(),
                "name=" + saved.getName());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponseDTO getById(Long id) {
        Patient patient = repository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));
        checkPatientOwnership(patient);
        return mapper.toResponse(patient);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "patients", key = "'profile_' + T(com.hms.common.util.SecurityUtils).getCurrentUsername()")
    public PatientResponseDTO getCurrentPatientProfile() {
        User user = currentUser();
        Patient patient = repository.findByEmail(user.getEmail())
                .orElseThrow(() -> new PatientNotFoundException("Your patient profile is not linked yet."));
        return mapper.toResponse(patient);
    }

    @Override
    @CacheEvict(value = "patients", allEntries = true)
    public PatientResponseDTO update(Long id, PatientRequestDTO dto) {
        Patient patient = repository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));
        checkPatientOwnership(patient);

        if (!patient.getContactNumber().equals(dto.getContactNumber())
                && repository.existsByContactNumber(dto.getContactNumber())) {
            throw new DuplicatePatientException("Contact number already used");
        }

        mapper.updateEntity(dto, patient);
        Patient updated = repository.save(patient);
        log.info("Patient updated successfully with ID: {}", id);
        auditLogService.log(SecurityUtils.getCurrentUsername(), "PATIENT_UPDATE", "Patient", id.toString(),
                "name=" + updated.getName());
        return mapper.toResponse(updated);
    }

    @Transactional
    @Override
    @CacheEvict(value = "patients", allEntries = true)
    public void delete(Long id) {
        Patient patient = repository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));

        long activeAppointments = appointmentRepository.countByPatientIdAndStatusIn(id,
                java.util.List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED, AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_CONSULTATION));

        if (activeAppointments > 0) {
            throw new BadRequestException("Cannot delete patient with active or upcoming appointments. Please transition or cancel them first.");
        }

        repository.delete(patient);
        log.info("Patient deleted with ID: {}", id);
        auditLogService.log(SecurityUtils.getCurrentUsername(), "PATIENT_DELETE", "Patient", id.toString(),
                "name=" + patient.getName());

        userRepository.findByEmail(patient.getEmail())
                .ifPresent(user -> userService.deleteUser(user.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "patients", key = "'all'")
    public List<PatientResponseDTO> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "patients", key = "'slice_' + #page + '_' + #size")
    public Slice<PatientResponseDTO> getSlice(int page, int size) {
        PageRequest request = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repository.findAll(request).map(mapper::toResponse);
    }

    private void checkPatientOwnership(Patient patient) {
        User user = currentUser();
        if (user.getRole() != Role.PATIENT) {
            return;
        }

        if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(patient.getEmail())) {
            return;
        }

        throw new AccessDeniedException("You can only access your own patient profile.");
    }

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
