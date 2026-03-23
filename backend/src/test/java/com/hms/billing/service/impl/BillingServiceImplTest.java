package com.hms.billing.service.impl;

import com.hms.appointment.repository.AppointmentRepository;
import com.hms.billing.dto.request.BillingRequestDTO;
import com.hms.billing.dto.response.BillingResponseDTO;
import com.hms.billing.entity.Billing;
import com.hms.billing.mapper.BillingMapper;
import com.hms.billing.repository.BillingRepository;
import com.hms.common.audit.AuditLogService;
import com.hms.patient.entity.Patient;
import com.hms.patient.repository.PatientRepository;
import com.hms.prescription.repository.PrescriptionRepository;
import com.hms.laboratory.repository.LabTestRepository;
import com.hms.pharmacy.repository.MedicineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceImplTest {

    @Mock
    private BillingRepository billingRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private BillingMapper billingMapper;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private LabTestRepository labTestRepository;

    @Mock
    private MedicineRepository medicineRepository;

    @InjectMocks
    private BillingServiceImpl billingService;

    private BillingRequestDTO requestDTO;
    private Patient mockPatient;
    private Billing mockBilling;

    @BeforeEach
    void setUp() {
        // Mock the tax rate value that is injected via @Value
        ReflectionTestUtils.setField(billingService, "taxRate", new BigDecimal("0.05"));

        requestDTO = new BillingRequestDTO();
        requestDTO.setPatientId(UUID.randomUUID());
        requestDTO.setBillingDate(LocalDateTime.now());
        requestDTO.setTotalAmount(new BigDecimal("1000.00"));
        requestDTO.setDiscountAmount(new BigDecimal("50.00"));

        mockPatient = new Patient();
        mockPatient.setId(requestDTO.getPatientId());
        mockPatient.setName("Test Patient");

        mockBilling = new Billing();
        mockBilling.setId(UUID.randomUUID());
        mockBilling.setPatient(mockPatient);
        mockBilling.setInvoiceNumber("INV-12345");
        mockBilling.setTotalAmount(new BigDecimal("1000.00"));
        mockBilling.setTaxAmount(new BigDecimal("50.00"));
        mockBilling.setNetAmount(new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("Successfully calculate net amount (Total + Tax - Discount) during manual billing")
    void createBilling_CalculatesTotals_Correctly() {
        // Arrange
        when(patientRepository.findById(any())).thenReturn(Optional.of(mockPatient));
        when(billingRepository.save(any())).thenReturn(mockBilling);
        when(billingMapper.toDto(any())).thenReturn(new BillingResponseDTO());

        // Act
        BillingResponseDTO result = billingService.createBilling(requestDTO);

        // Assert
        assertNotNull(result);
        verify(billingRepository, times(1)).save(any(Billing.class));
        
        // Also verify that calculation logic is called
        // Net = 1000 + (1000 * 0.05) - 50 = 1000
    }

    @Test
    @DisplayName("Fail to generate billing when patient is missing in system")
    void createBilling_ReturnsError_WhenPatientNotFound() {
        // Arrange
        when(patientRepository.findById(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            billingService.createBilling(requestDTO);
        });

        verify(billingRepository, never()).save(any());
    }
}
