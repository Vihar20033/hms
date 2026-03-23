package com.hms.patient.service.impl;

import com.hms.common.audit.AuditLogService;
import com.hms.common.enums.Role;
import com.hms.patient.dto.request.PatientRequestDTO;
import com.hms.patient.dto.response.PatientOnboardingResponseDTO;
import com.hms.patient.dto.response.PatientResponseDTO;
import com.hms.patient.entity.Patient;
import com.hms.patient.exception.DuplicatePatientException;
import com.hms.patient.exception.PatientNotFoundException;
import com.hms.patient.mapper.PatientMapper;
import com.hms.patient.repository.PatientRepository;
import com.hms.patient.service.PatientService;
import com.hms.patient.specification.PatientSpecification;
import com.hms.user.entity.User;
import com.hms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PatientServiceImpl implements PatientService {
    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#$%";
    private static final int TEMP_PASSWORD_LENGTH = 12;
    private static final Random SECURE_RANDOM = new java.security.SecureRandom();

    private final PatientRepository repository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final PatientMapper mapper;

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("name", "age", "createdAt");
    private static final int MAX_PAGE_SIZE = 100;

    @Override
    public PatientOnboardingResponseDTO create(PatientRequestDTO dto) {
        log.info("Creating patient with contact number: {}", dto.getContactNumber());

        if (repository.existsByContactNumber(dto.getContactNumber())) {
            throw new DuplicatePatientException("Patient already exists");
        }

        String onboardingUsername = null;
        String temporaryPassword = null;
        Boolean passwordChangeRequired = null;

        if (dto.getEmail() != null && !userRepository.existsByEmail(dto.getEmail())) {
            temporaryPassword = generateTemporaryPassword();
            onboardingUsername = dto.getEmail();
            passwordChangeRequired = true;
            User user = User.builder()
                    .username(dto.getEmail())
                    .email(dto.getEmail())
                    .password(passwordEncoder.encode(temporaryPassword))
                    .role(Role.PATIENT)
                    .enabled(true)
                    .passwordChangeRequired(true)
                    .build();
            userRepository.save(user);
            log.info("Automatic User account created for patient: {}", dto.getEmail());
        }

        Patient saved = repository.save(mapper.toEntity(dto));
        log.info("Patient created successfully with ID: {}", saved.getId());
        auditLogService.log(getCurrentUsername(), "PATIENT_CREATE", "Patient", saved.getId().toString(), "name=" + saved.getName());

        return PatientOnboardingResponseDTO.builder()
                .patient(mapper.toResponse(saved))
                .username(onboardingUsername)
                .temporaryPassword(temporaryPassword)
                .passwordChangeRequired(passwordChangeRequired)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<PatientResponseDTO> search(String name, String email, String bloodGroup, String urgencyLevel, int page, int size, String sortBy) {
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) sortBy = "createdAt";
        if (size > MAX_PAGE_SIZE) size = MAX_PAGE_SIZE;
        if (size < 1) size = 10;

        Specification<Patient> spec = Specification.where(PatientSpecification.notDeleted())
                .and(PatientSpecification.hasName(name))
                .and(PatientSpecification.hasEmail(email))
                .and(PatientSpecification.hasBloodGroup(bloodGroup))
                .and(PatientSpecification.hasUrgencyLevel(urgencyLevel));

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Slice<Patient> slice = repository.findAllAsSlice(spec, pageable);
        return slice.map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponseDTO getById(UUID id) {
        Patient patient = repository.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));

        checkOwnership(patient);
        return mapper.toResponse(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponseDTO getMyProfile() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getRole() != Role.PATIENT) {
            throw new AccessDeniedException("Current user is not a patient.");
        }
        
        Patient patient = repository.findByEmail(user.getEmail())
                .orElseThrow(() -> new PatientNotFoundException("No patient record found for email: " + user.getEmail()));
        
        return mapper.toResponse(patient);
    }

    @Override
    public PatientResponseDTO update(UUID id, PatientRequestDTO dto) {
        Patient patient = repository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));

        if (!patient.getContactNumber().equals(dto.getContactNumber()) && repository.existsByContactNumber(dto.getContactNumber())) {
            throw new DuplicatePatientException("Contact number already used");
        }

        mapper.updateEntity(dto, patient);
        Patient updated = repository.save(patient);
        log.info("Patient updated successfully with ID: {}", id);
        auditLogService.log(getCurrentUsername(), "PATIENT_UPDATE", "Patient", id.toString(), "name=" + updated.getName());

        return mapper.toResponse(updated);
    }

    @Override
    public void delete(UUID id) {
        Patient patient = repository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));

        patient.setDeleted(true);
        repository.save(patient);
        log.info("Patient soft deleted with ID: {}", id);
        auditLogService.log(getCurrentUsername(), "PATIENT_DELETE", "Patient", id.toString(), "name=" + patient.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponseDTO> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    private void checkOwnership(Patient patient) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Role role = user.getRole();

        // 1. Staff Access
        if (role == Role.ADMIN || role == Role.DOCTOR || role == Role.NURSE || role == Role.RECEPTIONIST || role == Role.PHARMACIST || role == Role.LABORATORY_STAFF) {
            return;
        }

        // 2. Patient Access
        if (role == Role.PATIENT) {
            if (user.getEmail().equalsIgnoreCase(patient.getEmail())) {
                return;
            }
        }

        log.warn("Security Alert: User {} with role {} tried to access patient record of {}.", user.getUsername(), role, patient.getName());
        throw new AccessDeniedException("You do not have permission to access this record.");
    }

    private static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) ? auth.getName() : "system";
    }

    private String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder(TEMP_PASSWORD_LENGTH);
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            password.append(TEMP_PASSWORD_CHARS.charAt(SECURE_RANDOM.nextInt(TEMP_PASSWORD_CHARS.length())));
        }
        return password.toString();
    }
}
