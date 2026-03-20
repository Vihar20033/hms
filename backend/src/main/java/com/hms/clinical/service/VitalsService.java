package com.hms.clinical.service;

import com.hms.clinical.dto.request.VitalsRequestDTO;
import com.hms.clinical.dto.response.VitalsResponseDTO;

import java.util.UUID;

import java.util.List;

public interface VitalsService {
    VitalsResponseDTO recordVitals(VitalsRequestDTO dto);
    VitalsResponseDTO updateVitals(UUID id, VitalsRequestDTO dto);
    VitalsResponseDTO getVitalsByAppointment(UUID appointmentId);
    List<VitalsResponseDTO> getVitalsByPatientId(UUID patientId);
    void deleteVitals(UUID id);
    List<VitalsResponseDTO> getAllVitals();
}
