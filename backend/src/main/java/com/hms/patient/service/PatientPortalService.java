package com.hms.patient.service;

import com.hms.patient.dto.response.PatientPortalSummaryResponse;

public interface PatientPortalService {
    PatientPortalSummaryResponse getCurrentPatientSummary();
}
