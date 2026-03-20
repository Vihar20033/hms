package com.hms.patient.service.impl;

import com.hms.common.audit.AuditLogService;
import com.hms.common.enums.BloodGroup;
import com.hms.common.enums.Role;
import com.hms.common.enums.UrgencyLevel;
import com.hms.patient.dto.request.PatientRequestDTO;
import com.hms.patient.dto.response.PatientOnboardingResponseDTO;
import com.hms.patient.entity.Patient;
import com.hms.patient.mapper.PatientMapper;
import com.hms.patient.repository.PatientRepository;
import com.hms.user.entity.User;
import com.hms.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private PatientMapper mapper;

    private PatientServiceImpl patientService;

    @BeforeEach
    void setUp() {
        patientService = new PatientServiceImpl(repository, userRepository, passwordEncoder, auditLogService, mapper);
    }

    @Test
    void createGeneratesTemporaryPasswordAndForcesPasswordChangeForPatientUser() {
        PatientRequestDTO request = new PatientRequestDTO();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setAge(32);
        request.setBloodGroup(BloodGroup.A_POSITIVE);
        request.setPrescription("Observation");
        request.setDose("Rest");
        request.setFees(new BigDecimal("200"));
        request.setContactNumber("9876543210");
        request.setUrgencyLevel(UrgencyLevel.LOW);

        Patient patientEntity = new Patient();
        patientEntity.setName("John Doe");

        Patient savedPatient = new Patient();
        savedPatient.setId(UUID.randomUUID());
        savedPatient.setName("John Doe");

        when(repository.existsByContactNumber("9876543210")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(patientEntity);
        when(repository.save(patientEntity)).thenReturn(savedPatient);
        when(mapper.toResponse(savedPatient)).thenReturn(com.hms.patient.dto.response.PatientResponseDTO.builder()
                .name("John Doe")
                .contactNumber("9876543210")
                .build());
        when(passwordEncoder.encode(any(String.class))).thenAnswer(invocation -> "encoded:" + invocation.getArgument(0, String.class));

        PatientOnboardingResponseDTO response = patientService.create(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        org.mockito.Mockito.verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(response.getUsername()).isEqualTo("john@example.com");
        assertThat(response.getTemporaryPassword()).isNotBlank().hasSize(12);
        assertThat(response.getPasswordChangeRequired()).isTrue();
        assertThat(savedUser.getRole()).isEqualTo(Role.PATIENT);
        assertThat(savedUser.getPasswordChangeRequired()).isTrue();
        assertThat(savedUser.getPassword()).startsWith("encoded:");
        assertThat(savedUser.getPassword()).doesNotContain("Patient@123");
    }
}
