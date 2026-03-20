package com.hms.patient.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientOnboardingResponseDTO {
    private PatientResponseDTO patient;
    private String username;
    private String temporaryPassword;
    private Boolean passwordChangeRequired;
}
