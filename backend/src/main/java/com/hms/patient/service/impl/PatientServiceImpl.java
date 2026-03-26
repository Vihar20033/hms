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
import com.hms.patient.specification.PatientSpecification;
import com.hms.user.repository.UserRepository;
import com.hms.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
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

    // Prevents Invalid Sorting  , Large Page Size performance issue -> Defensive programing
    private static final List<String> ALLOWED_SORT_FIELDS = List.of("name", "age", "createdAt");
    private static final int MAX_PAGE_SIZE = 100;

    @Override
    public PatientResponseDTO create(PatientRequestDTO dto) {

        log.info("Creating patient with contact number: {}", dto.getContactNumber());
        if (repository.existsByContactNumber(dto.getContactNumber())) {
            throw new DuplicatePatientException("Patient already exists");
        }

        Patient saved = repository.save(mapper.toEntity(dto));
        log.info("Patient created successfully with ID: {}", saved.getId());
        auditLogService.log(SecurityUtils.getCurrentUsername(), "PATIENT_CREATE", "Patient", saved.getId().toString(), "name=" + saved.getName());

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<PatientResponseDTO> search(String query, String name, String email, String bloodGroup, String urgencyLevel, int page, int size, String sortBy) {
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) sortBy = "createdAt";
        if (size > MAX_PAGE_SIZE) size = MAX_PAGE_SIZE;
        if (size < 1) size = 10;

        Specification<Patient> spec = Specification.where(PatientSpecification.fuzzySearch(query))
                .and(PatientSpecification.fuzzySearch(query))
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
    public PatientResponseDTO getById(Long id) {
        Patient patient = repository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));

        return mapper.toResponse(patient);
    }

    @Override
    public PatientResponseDTO update(Long id, PatientRequestDTO dto) {
        Patient patient = repository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));

        if (!patient.getContactNumber().equals(dto.getContactNumber()) && repository.existsByContactNumber(dto.getContactNumber())) {
            throw new DuplicatePatientException("Contact number already used");
        }

        mapper.updateEntity(dto, patient);
        Patient updated = repository.save(patient);
        log.info("Patient updated successfully with ID: {}", id);
        auditLogService.log(SecurityUtils.getCurrentUsername(), "PATIENT_UPDATE", "Patient", id.toString(), "name=" + updated.getName());

        return mapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        Patient patient = repository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));

        repository.delete(patient);
        log.info("Patient soft deleted with ID: {}", id);
        auditLogService.log(SecurityUtils.getCurrentUsername(), "PATIENT_DELETE", "Patient", id.toString(), "name=" + patient.getName());

        // Linked User Deletion => If patient has user account → delete it
        userRepository.findByEmail(patient.getEmail()).ifPresent(user -> {
            userService.deleteUser(user.getId());
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponseDTO> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }
}
