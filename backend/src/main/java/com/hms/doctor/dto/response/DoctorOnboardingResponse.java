package com.hms.doctor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorOnboardingResponse {
    private DoctorResponseDTO doctor;
    private String username;
    private String temporaryPassword;
    private Boolean passwordChangeRequired;
}
