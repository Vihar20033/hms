package com.hms.patient.service;

import com.hms.patient.dto.request.PatientRequestDTO;
import com.hms.patient.dto.response.PatientResponseDTO;
import org.springframework.data.domain.Slice;

import java.util.List;


public interface PatientService {

    PatientResponseDTO create(PatientRequestDTO dto);

    PatientResponseDTO getById(Long id);

    PatientResponseDTO update(Long id, PatientRequestDTO dto);

    void delete(Long id);

    List<PatientResponseDTO> getAll();

    Slice<PatientResponseDTO> getSlice(int page, int size);

    Slice<PatientResponseDTO> getSearchableSlice(int page, int size, String query);
}
