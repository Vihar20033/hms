package com.hms.laboratory.service;

import com.hms.laboratory.dto.request.LabReportRequestDTO;
import com.hms.laboratory.dto.request.LabTestRequestDTO;
import com.hms.laboratory.dto.response.LabReportResponseDTO;
import com.hms.laboratory.dto.response.LabTestResponseDTO;
import com.hms.common.enums.TestStatus;

import java.util.List;
import java.util.UUID;

public interface LabService {
    LabTestResponseDTO requestTest(LabTestRequestDTO dto);

    LabTestResponseDTO updateTest(UUID testId, LabTestRequestDTO dto);

    LabTestResponseDTO updateTestStatus(UUID testId, TestStatus status);

    LabTestResponseDTO getTestById(UUID id);

    List<LabTestResponseDTO> getAllTests();

    List<LabTestResponseDTO> getMyTests();

    List<LabTestResponseDTO> getTestsByPatientId(UUID patientId);

    void deleteTest(UUID id);

    // LabReport Service Methods
    LabReportResponseDTO createReport(LabReportRequestDTO dto);

    List<LabReportResponseDTO> getMyReports();

    LabReportResponseDTO updateReport(UUID reportId, LabReportRequestDTO dto);

    LabReportResponseDTO getReportByTestId(UUID testId);

    LabReportResponseDTO getReportById(UUID reportId);

    List<LabReportResponseDTO> getAllReports();

    void deleteReport(UUID reportId);
}


