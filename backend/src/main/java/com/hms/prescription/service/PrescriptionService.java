package com.hms.prescription.service;

import com.hms.prescription.dto.request.PrescriptionRequestDTO;
import com.hms.prescription.dto.response.PrescriptionResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PrescriptionService {
    PrescriptionResponseDTO createPrescription(PrescriptionRequestDTO dto);

    PrescriptionResponseDTO getPrescriptionById(UUID id);

    List<PrescriptionResponseDTO> getAllPrescriptions();



    List<PrescriptionResponseDTO> getPrescriptionsByPatientId(UUID patientId);

    List<PrescriptionResponseDTO> getPrescriptionsByDoctorId(UUID doctorId);

    void deletePrescription(UUID id);
}
