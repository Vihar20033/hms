package com.hms.appointment.service.impl;

import com.hms.appointment.dto.request.AppointmentRequestDTO;
import com.hms.appointment.entity.Appointment;
import com.hms.appointment.mapper.AppointmentMapper;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.common.audit.AuditLogService;
import com.hms.common.enums.AppointmentStatus;
import com.hms.common.enums.Department;
import com.hms.doctor.entity.Doctor;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.patient.entity.Patient;
import com.hms.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private AppointmentRequestDTO requestDTO;
    private Patient mockPatient;
    private Doctor mockDoctor;
    private Appointment mockAppointment;
    private final UUID patientId = UUID.randomUUID();
    private final UUID doctorId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        requestDTO = AppointmentRequestDTO.builder()
                .patientId(patientId)
                .doctorId(doctorId)
                .department(Department.CARDIOLOGY)
                .appointmentDate(LocalDate.now().plusDays(1))
                .appointmentTime(LocalTime.of(10, 0))
                .reason("Test Reason")
                .isEmergency(false)
                .build();

        mockPatient = Patient.builder().id(patientId).name("Test Patient").build();
        mockDoctor = Doctor.builder().id(doctorId).firstName("Test").lastName("Doctor").build();
        mockAppointment = Appointment.builder()
                .id(UUID.randomUUID())
                .patient(mockPatient)
                .doctor(mockDoctor)
                .appointmentTime(LocalDateTime.of(requestDTO.getAppointmentDate(), requestDTO.getAppointmentTime()))
                .build();
    }

    @Test
    @DisplayName("Successfully create an appointment with valid data")
    void createAppointment_Success() {
        // Arrange
        when(appointmentMapper.toEntity(any())).thenReturn(mockAppointment);
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(mockPatient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(mockDoctor));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(mockAppointment);
        when(appointmentRepository.countByDoctorIdAndAppointmentTimeBetween(any(), any(), any())).thenReturn(0L);

        // Act
        Appointment result = appointmentService.createAppointment(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(patientId, result.getPatient().getId());
        assertEquals(doctorId, result.getDoctor().getId());
        assertEquals(AppointmentStatus.SCHEDULED, result.getStatus());
        assertEquals("P-001", result.getTokenNumber());

        verify(appointmentRepository, times(1)).save(any(Appointment.class));
        verify(auditLogService, times(1)).log(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Fail to create appointment when patient does not exist")
    void createAppointment_PatientNotFound() {
        // Arrange
        when(appointmentMapper.toEntity(any())).thenReturn(mockAppointment);
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            appointmentService.createAppointment(requestDTO);
        });

        assertTrue(exception.getMessage().contains("Patient not found"));
        verify(appointmentRepository, never()).save(any());
    }
}
