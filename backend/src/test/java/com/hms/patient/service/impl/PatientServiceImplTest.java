package com.hms.patient.service.impl;

import com.hms.common.audit.AuditLogService;
import com.hms.patient.dto.request.PatientRequestDTO;
import com.hms.patient.dto.response.PatientOnboardingResponseDTO;
import com.hms.patient.entity.Patient;
import com.hms.patient.mapper.PatientMapper;
import com.hms.patient.repository.PatientRepository;
import com.hms.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PatientMapper mapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private PatientServiceImpl patientService;

    private PatientRequestDTO requestDTO;
    private Patient mockPatient;

    @BeforeEach
    void setUp() {
        requestDTO = new PatientRequestDTO();
        requestDTO.setName("John Doe");
        requestDTO.setEmail("john@example.com");
        requestDTO.setContactNumber("1234567890");

        mockPatient = new Patient();
        mockPatient.setId(UUID.randomUUID());
        mockPatient.setName("John Doe");
        mockPatient.setContactNumber("1234567890");
    }

    @Test
    @DisplayName("Should create patient correctly and log audit")
    void createPatient_Success() {
        // Arrange
        when(repository.existsByContactNumber(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(mapper.toEntity(any())).thenReturn(mockPatient);
        when(repository.save(any())).thenReturn(mockPatient);
        
        // Act
        PatientOnboardingResponseDTO result = patientService.create(requestDTO);

        // Assert
        assertNotNull(result);
        verify(repository).save(any());
        verify(auditLogService).log(any(), eq("PATIENT_CREATE"), eq("Patient"), any(), any());
    }

    @Test
    @DisplayName("Should throw exception if contact number is already registered")
    void createPatient_ContactExists() {
        // Arrange
        when(repository.existsByContactNumber("1234567890")).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> patientService.create(requestDTO));
        verify(repository, never()).save(any());
    }
}
