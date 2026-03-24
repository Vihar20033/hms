package com.hms.laboratory.service.impl;

import com.hms.appointment.entity.Appointment;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.common.audit.AuditLogService;
import com.hms.common.enums.Role;
import com.hms.common.enums.TestStatus;
import com.hms.common.exception.BadRequestException;
import com.hms.doctor.entity.Doctor;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.laboratory.dto.request.LabReportRequestDTO;
import com.hms.laboratory.dto.request.LabTestRequestDTO;
import com.hms.laboratory.dto.response.LabReportResponseDTO;
import com.hms.laboratory.dto.response.LabTestResponseDTO;
import com.hms.laboratory.entity.LabReport;
import com.hms.laboratory.entity.LabTest;
import com.hms.laboratory.mapper.LabMapper;
import com.hms.laboratory.repository.LabReportRepository;
import com.hms.laboratory.repository.LabTestRepository;
import com.hms.laboratory.service.LabService;
import com.hms.patient.entity.Patient;
import com.hms.patient.repository.PatientRepository;
import com.hms.user.entity.User;
import com.hms.user.dto.UserResponseDTO;
import com.hms.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LabServiceImpl implements LabService {

    private final LabTestRepository labTestRepository;
    private final LabReportRepository labReportRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserService userService;
    private final LabMapper labMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public LabTestResponseDTO requestTest(LabTestRequestDTO dto) {

        if (dto.getPrice() == null) {
            throw new BadRequestException("Lab test price is required");
        }
        if (dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Lab test price must be greater than zero");
        }
        if (dto.getPatientId() == null) {
            throw new BadRequestException("Patient ID is required");
        }

        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        LabTest labTest = labMapper.toEntity(dto);
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
                        .orElseGet(() -> doctorRepository.save(Doctor.builder()
                            .userId(currentUser.getId())
                            .firstName(currentUser.getUsername())
                            .lastName("(Auto-Generated)")
                            .specialization("General")
                            .registrationNumber("TEMP-" + UUID.randomUUID().toString().substring(0, 8))
                            .email(currentUser.getEmail())
                            .isAvailable(true)
                            .build()));
                labTest.setRequestedBy(doctor);
            }
        }

        if (labTest.getRequestedBy() == null) {
            throw new BadRequestException("A valid requesting doctor is required for all lab tests.");
        }
        
        return labMapper.toDto(labTestRepository.save(labTest));
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
        LabTest test = labTestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lab test not found"));
        
        checkOwnership(test);
        return labMapper.toDto(test);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabTestResponseDTO> getAllTests() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Role role = user.getRole();

        if (role == Role.ADMIN || role == Role.LABORATORY_STAFF) {
            return labMapper.toDtoList(labTestRepository.findAll());
        }

        if (role == Role.DOCTOR) {
            return labMapper.toDtoList(labTestRepository.findByRequestedByUserId(user.getId()));
        }

        return Collections.emptyList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<LabTestResponseDTO> getTestsByPatientId(UUID patientId) {
        List<LabTest> tests = labTestRepository.findByPatientId(patientId);
        if (!tests.isEmpty()) {
            checkOwnership(tests.get(0));
        }
        return labMapper.toDtoList(tests);
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
        } else {
            report = labMapper.toReportEntity(dto);
            report.setLabTest(labTest);
            labTest.setReport(report);
        }
        
        labTest.setStatus(TestStatus.COMPLETED);
        labTest.setCompletedDate(LocalDateTime.now());
        
        LabTest savedTest = labTestRepository.save(labTest);
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

        return labMapper.toReportDto(labReportRepository.save(report));
    }


    @Override
    @Transactional(readOnly = true)
    public LabReportResponseDTO getReportByTestId(UUID testId) {
        LabReport report = labReportRepository.findByLabTestId(testId)
                .orElseThrow(() -> new RuntimeException("Lab report not found"));
        
        checkOwnership(report.getLabTest());
        return labMapper.toReportDto(report);
    }

    @Override
    @Transactional(readOnly = true)
    public LabReportResponseDTO getReportById(UUID reportId) {
        LabReport report = labReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Lab report not found"));
        
        checkOwnership(report.getLabTest());
        return labMapper.toReportDto(report);
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
    }

    private void checkOwnership(LabTest test) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Role role = user.getRole();

        // 1. Staff
        if (role == Role.ADMIN || role == Role.LABORATORY_STAFF) return;

        // 2. Doctor (If they requested it or are assigned)
        if (role == Role.DOCTOR) {
            if (test.getRequestedBy() != null && test.getRequestedBy().getUserId().equals(user.getId())) return;
        }


        log.warn("Security Alert: User {} with role {} tried to access lab result {}.", user.getUsername(), role, test.getId());
        throw new AccessDeniedException("You do not have permission to view these laboratory results.");
    }

    private String getCurrentUsername() {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }
}
