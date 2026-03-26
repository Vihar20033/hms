package com.hms.clinical.service;

import com.hms.clinical.dto.request.VitalsRequestDTO;
import com.hms.clinical.dto.response.VitalsResponseDTO;



import java.util.List;

public interface VitalsService {
    VitalsResponseDTO recordVitals(VitalsRequestDTO dto);
    VitalsResponseDTO updateVitals(Long id, VitalsRequestDTO dto);
    VitalsResponseDTO getVitalsByAppointment(Long appointmentId);
    List<VitalsResponseDTO> getVitalsByPatientId(Long patientId);
    void deleteVitals(Long id);
    List<VitalsResponseDTO> getAllVitals();
}
