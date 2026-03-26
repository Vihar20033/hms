package com.hms.patient.service;

import com.hms.patient.dto.request.PatientRequestDTO;
import com.hms.patient.dto.request.PatientSearchCriteria;
import com.hms.patient.dto.response.PatientResponseDTO;
import org.springframework.data.domain.Slice;

import java.util.List;


public interface PatientService {

    PatientResponseDTO create(PatientRequestDTO dto);

    Slice<PatientResponseDTO> search(
            PatientSearchCriteria criteria,
            int page,
            int size,
            String sortBy
    );

    PatientResponseDTO getById(Long id);

    PatientResponseDTO update(Long id, PatientRequestDTO dto);

    void delete(Long id);

    List<PatientResponseDTO> getAll();
}
