package com.hms.prescription.service;

import com.hms.prescription.dto.request.PrescriptionRequestDTO;
import com.hms.prescription.dto.response.PrescriptionResponseDTO;
import org.springframework.data.domain.Slice;

import java.util.List;


public interface PrescriptionService {
    PrescriptionResponseDTO createPrescription(PrescriptionRequestDTO dto);

    PrescriptionResponseDTO getPrescriptionById(Long id);

    List<PrescriptionResponseDTO> getAllPrescriptions();

    Slice<PrescriptionResponseDTO> getPrescriptionSlice(int page, int size);

    Slice<PrescriptionResponseDTO> getPrescriptionSlice(int page, int size, String query);

    List<PrescriptionResponseDTO> getPrescriptionsByPatientId(Long patientId);

    void deletePrescription(Long id);
}
