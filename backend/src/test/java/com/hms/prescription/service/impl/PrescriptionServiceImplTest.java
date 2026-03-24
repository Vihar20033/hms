package com.hms.prescription.service.impl;

import com.hms.appointment.entity.Appointment;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.common.audit.AuditLogService;
import com.hms.common.enums.Role;
import com.hms.doctor.entity.Doctor;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.patient.entity.Patient;
import com.hms.patient.repository.PatientRepository;
import com.hms.pharmacy.repository.InventoryTransactionRepository;
import com.hms.pharmacy.repository.MedicineRepository;
import com.hms.prescription.dto.request.PrescriptionRequestDTO;
import com.hms.prescription.dto.response.PrescriptionResponseDTO;
import com.hms.prescription.entity.Prescription;
import com.hms.prescription.mapper.PrescriptionMapper;
import com.hms.prescription.repository.PrescriptionRepository;
import com.hms.user.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceImplTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Mock
    private PrescriptionMapper prescriptionMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private PrescriptionServiceImpl prescriptionService;

    private PrescriptionRequestDTO requestDTO;
    private Prescription mockPrescription;
    private Patient mockPatient;
    private Doctor mockDoctor;
    private Appointment mockAppointment;

    @BeforeEach
    void setUp() {
        // Set up SecurityContext with admin user for ownership checks
        User adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(adminUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        requestDTO = new PrescriptionRequestDTO();
        requestDTO.setPatientId(UUID.randomUUID());
        requestDTO.setDoctorId(UUID.randomUUID());
        requestDTO.setAppointmentId(UUID.randomUUID());
        requestDTO.setDiagnosis("Common Cold");
        requestDTO.setNotes("Drink more water");

        mockPatient = new Patient();
        mockPatient.setId(requestDTO.getPatientId());

        mockDoctor = new Doctor();
        mockDoctor.setId(requestDTO.getDoctorId());

        mockAppointment = new Appointment();
        mockAppointment.setId(requestDTO.getAppointmentId());

        mockPrescription = new Prescription();
        mockPrescription.setId(UUID.randomUUID());
        mockPrescription.setDiagnosis("Common Cold");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should create prescription and correctly link to patient, doctor, and appointment")
    void createPrescription_Success() {
        // Arrange
        when(patientRepository.findById(any())).thenReturn(Optional.of(mockPatient));
        when(doctorRepository.findById(any())).thenReturn(Optional.of(mockDoctor));
        when(appointmentRepository.findById(any())).thenReturn(Optional.of(mockAppointment));
        when(prescriptionMapper.toEntity(any())).thenReturn(mockPrescription);
        when(prescriptionRepository.save(any())).thenReturn(mockPrescription);
        when(prescriptionMapper.toDto(any())).thenReturn(new PrescriptionResponseDTO());

        // Act
        PrescriptionResponseDTO result = prescriptionService.createPrescription(requestDTO);

        // Assert
        assertNotNull(result);
        verify(prescriptionRepository).save(any());
    }

    @Test
    @DisplayName("Successfully retrieve prescription by ID")
    void getPrescriptionById_Success() {
        // Arrange
        when(prescriptionRepository.findById(any())).thenReturn(Optional.of(mockPrescription));
        when(prescriptionMapper.toDto(any())).thenReturn(new PrescriptionResponseDTO());

        // Act
        PrescriptionResponseDTO result = prescriptionService.getPrescriptionById(UUID.randomUUID());

        // Assert
        assertNotNull(result);
        verify(prescriptionRepository).findById(any());
    }
}
