package com.hms.patient.service;

import com.hms.patient.dto.request.PatientRequestDTO;
import com.hms.patient.dto.response.PatientOnboardingResponseDTO;
import com.hms.patient.dto.response.PatientResponseDTO;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.UUID;

public interface PatientService {

    PatientOnboardingResponseDTO create(PatientRequestDTO dto);

    Slice<PatientResponseDTO> search(
            String name,
            String email,
            String bloodGroup,
            String urgencyLevel,
            int page,
            int size,
            String sortBy
    );

    PatientResponseDTO getById(UUID id);

    PatientResponseDTO update(UUID id, PatientRequestDTO dto);

    void delete(UUID id);

    List<PatientResponseDTO> getAll();
}
