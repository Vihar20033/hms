package com.hms.laboratory.service.impl;

import com.hms.appointment.entity.Appointment;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.common.exception.BadRequestException;
import com.hms.doctor.entity.Doctor;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.laboratory.dto.request.LabReportRequestDTO;
import com.hms.laboratory.dto.request.LabTestRequestDTO;
import com.hms.laboratory.dto.response.LabReportResponseDTO;
import com.hms.laboratory.dto.response.LabTestResponseDTO;
import com.hms.laboratory.entity.LabReport;
import com.hms.laboratory.entity.LabTest;
import com.hms.common.enums.TestStatus;
import com.hms.laboratory.mapper.LabMapper;
import com.hms.laboratory.repository.LabReportRepository;
import com.hms.laboratory.repository.LabTestRepository;
import com.hms.laboratory.service.LabService;
import com.hms.patient.entity.Patient;
import com.hms.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import com.hms.user.dto.UserResponseDTO;
import com.hms.user.service.UserService;
import com.hms.common.enums.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LabServiceImpl implements LabService {

    private final LabTestRepository labTestRepository;
    private final LabReportRepository labReportRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserService userService;
    private final LabMapper labMapper;
    private final com.hms.common.audit.AuditLogService auditLogService;

    @Override
    @Transactional
    public LabTestResponseDTO requestTest(LabTestRequestDTO dto) {
        if (dto.getPrice() == null) {
            throw new BadRequestException("Lab test price is required");
        }
        if (dto.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Lab test price must be greater than zero");
        }
        if (dto.getPatientId() == null) {
            throw new BadRequestException("Patient ID is required");
        }

        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        LabTest labTest = labMapper.toEntity(dto);
        labTest.setTestName(dto.getTestName());
        labTest.setTestCode(dto.getTestCode());
        labTest.setPrice(dto.getPrice());
        labTest.setDescription(dto.getDescription());
        labTest.setCategory(dto.getCategory());
        labTest.setPatient(patient);
        labTest.setStatus(TestStatus.PENDING);
        labTest.setRequestedDate(LocalDateTime.now());

        if (dto.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            labTest.setAppointment(appointment);
        }

        if (dto.getDoctorId() != null) {
            Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));
            labTest.setRequestedBy(doctor);
        } else {
            UserResponseDTO currentUser = userService.getCurrentUser();
            if (currentUser != null && currentUser.getRole() == Role.DOCTOR) {
                Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                        .orElse(null);
                
                if (doctor == null) {
                    doctor = Doctor.builder()
                            .userId(currentUser.getId())
                            .firstName(currentUser.getUsername())
                            .lastName("(Auto-Generated)")
                            .specialization("General")
                            .registrationNumber("TEMP-" + currentUser.getId().toString().substring(0, 8))
                            .email(currentUser.getEmail())
                            .isAvailable(true)
                            .build();
                    doctor = doctorRepository.save(doctor);
                }
                
                labTest.setRequestedBy(doctor);
            }
        }

        if (labTest.getRequestedBy() == null) {
            throw new BadRequestException("A valid requesting doctor is required for all lab tests.");
        }
        
        return labMapper.toDto(labTestRepository.saveAndFlush(labTest));
    }

    @Override
    @Transactional
    public LabTestResponseDTO updateTest(UUID testId, LabTestRequestDTO dto) {
        LabTest labTest = labTestRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Lab test not found: " + testId));

        if (dto.getTestName() != null) labTest.setTestName(dto.getTestName());
        if (dto.getTestCode() != null) labTest.setTestCode(dto.getTestCode());
        if (dto.getPrice() != null) labTest.setPrice(dto.getPrice());
        if (dto.getDescription() != null) labTest.setDescription(dto.getDescription());
        if (dto.getCategory() != null) labTest.setCategory(dto.getCategory());

        if (dto.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            labTest.setAppointment(appointment);
        }

        LabTest saved = labTestRepository.save(labTest);
        auditLogService.log(getCurrentUsername(), "LAB_TEST_UPDATE", "LabTest", testId.toString(), "updated");
        return labMapper.toDto(saved);
    }

    @Override
    @Transactional
    public LabTestResponseDTO updateTestStatus(UUID testId, TestStatus status) {
        LabTest labTest = labTestRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Lab test not found"));

        labTest.setStatus(status);
        if (status == TestStatus.COMPLETED) {
            labTest.setCompletedDate(LocalDateTime.now());
        }
        
        LabTest saved = labTestRepository.save(labTest);
        auditLogService.log(getCurrentUsername(), "LAB_TEST_STATUS_UPDATE", "LabTest", testId.toString(), "status=" + status);
        return labMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LabTestResponseDTO getTestById(UUID id) {
        return labTestRepository.findById(id)
                .map(labMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Lab test not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabTestResponseDTO> getAllTests() {
        return labMapper.toDtoList(labTestRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabTestResponseDTO> getTestsByPatientId(UUID patientId) {
        return labMapper.toDtoList(labTestRepository.findByPatientId(patientId));
    }

    @Override
    @Transactional
    public void deleteTest(UUID id) {
        LabTest labTest = labTestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lab test not found"));
        labTest.setDeleted(true);
        labTestRepository.save(labTest);
        auditLogService.log(getCurrentUsername(), "LAB_TEST_DELETE", "LabTest", id.toString(), "deleted=true");
    }

    @Override
    @Transactional
    public LabReportResponseDTO createReport(LabReportRequestDTO dto) {
        LabTest labTest = labTestRepository.findById(dto.getLabTestId())
                .orElseThrow(() -> new RuntimeException("Lab test not found"));

        Optional<LabReport> existingReportOpt = labReportRepository.findByLabTestId(dto.getLabTestId());

        LabReport report;
        if (existingReportOpt.isPresent()) {
            report = existingReportOpt.get();
            report.setFindings(dto.getFindings());
            report.setResult(dto.getResult());
            report.setUnit(dto.getUnit());
            report.setReferenceRange(dto.getReferenceRange());
            report.setRemarks(dto.getRemarks());
            report.setPerformedBy(dto.getPerformedBy());
            report.setDeleted(false);
        } else {
            report = labMapper.toReportEntity(dto);
            report.setLabTest(labTest);
            labTest.setReport(report);
        }
        
        labTest.setStatus(TestStatus.COMPLETED);
        labTest.setCompletedDate(LocalDateTime.now());
        
        LabTest savedTest = labTestRepository.save(labTest);
        auditLogService.log(getCurrentUsername(), "LAB_REPORT_CREATE", "LabReport", savedTest.getReport().getId().toString(), "test=" + savedTest.getTestName());
        return labMapper.toReportDto(savedTest.getReport());
    }

    @Override
    @Transactional
    public LabReportResponseDTO updateReport(UUID reportId, LabReportRequestDTO dto) {
        LabReport report = labReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Lab report not found"));

        report.setFindings(dto.getFindings());
        report.setResult(dto.getResult());
        report.setUnit(dto.getUnit());
        report.setReferenceRange(dto.getReferenceRange());
        report.setRemarks(dto.getRemarks());
        report.setPerformedBy(dto.getPerformedBy());

        LabReport saved = labReportRepository.save(report);
        auditLogService.log(getCurrentUsername(), "LAB_REPORT_UPDATE", "LabReport", reportId.toString(), "result=" + saved.getResult());
        return labMapper.toReportDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LabReportResponseDTO getReportByTestId(UUID testId) {
        return labReportRepository.findByLabTestId(testId)
                .map(labMapper::toReportDto)
                .orElseThrow(() -> new RuntimeException("Lab report not found for test ID: " + testId));
    }

    @Override
    @Transactional(readOnly = true)
    public LabReportResponseDTO getReportById(UUID reportId) {
        return labReportRepository.findById(reportId)
                .map(labMapper::toReportDto)
                .orElseThrow(() -> new RuntimeException("Lab report not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabReportResponseDTO> getAllReports() {
        return labMapper.toReportDtoList(labReportRepository.findAll());
    }

    @Override
    @Transactional
    public void deleteReport(UUID reportId) {
        LabReport report = labReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Lab report not found"));
        report.setDeleted(true);
        labReportRepository.save(report);
        auditLogService.log(getCurrentUsername(), "LAB_REPORT_DELETE", "LabReport", reportId.toString(), "deleted=true");
    }

    private String getCurrentUsername() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }
}

