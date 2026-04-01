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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
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
        return mapper.toResponse(patient);
    }

    @Override
    public PatientResponseDTO update(Long id, PatientRequestDTO dto) {
        Patient patient = repository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));

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
    public void delete(Long id) {
        Patient patient = repository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));

        repository.delete(patient);
        log.info("Patient deleted with ID: {}", id);
        auditLogService.log(SecurityUtils.getCurrentUsername(), "PATIENT_DELETE", "Patient", id.toString(),
                "name=" + patient.getName());

        userRepository.findByEmail(patient.getEmail())
                .ifPresent(user -> userService.deleteUser(user.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponseDTO> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }
}
