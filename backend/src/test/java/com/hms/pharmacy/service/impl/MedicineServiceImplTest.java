package com.hms.pharmacy.service.impl;

import com.hms.pharmacy.dto.request.MedicineRequestDTO;
import com.hms.pharmacy.dto.response.MedicineResponseDTO;
import com.hms.pharmacy.entity.Medicine;
import com.hms.pharmacy.exception.DuplicateMedicineException;
import com.hms.pharmacy.mapper.MedicineMapper;
import com.hms.pharmacy.repository.InventoryTransactionRepository;
import com.hms.pharmacy.repository.MedicineRepository;
import com.hms.common.audit.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicineServiceImplTest {

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private MedicineMapper medicineMapper;

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private MedicineServiceImpl medicineService;

    private MedicineRequestDTO requestDTO;
    private Medicine mockMedicine;

    @BeforeEach
    void setUp() {
        requestDTO = new MedicineRequestDTO();
        requestDTO.setName("Paracetamol");
        requestDTO.setMedicineCode("PARA-500");
        requestDTO.setQuantityInStock(100);
        requestDTO.setUnitPrice(new BigDecimal("10.50"));

        mockMedicine = new Medicine();
        mockMedicine.setId(UUID.randomUUID());
        mockMedicine.setName("Paracetamol");
        mockMedicine.setMedicineCode("PARA-500");
        mockMedicine.setQuantityInStock(100);
        mockMedicine.setUnitPrice(new BigDecimal("10.50"));
    }

    @Test
    @DisplayName("Should successfully create a new medicine")
    void createMedicine_Success() {
        // Arrange
        when(medicineRepository.existsByMedicineCode("PARA-500")).thenReturn(false);
        when(medicineMapper.toEntity(any())).thenReturn(mockMedicine);
        when(medicineRepository.save(any())).thenReturn(mockMedicine);
        when(medicineMapper.toDto(any())).thenReturn(new MedicineResponseDTO());

        // Act
        MedicineResponseDTO result = medicineService.createMedicine(requestDTO);

        // Assert
        assertNotNull(result);
        verify(medicineRepository, times(1)).save(any());
        verify(inventoryTransactionRepository, times(1)).save(any()); // Checks initial stock logging
    }

    @Test
    @DisplayName("Should throw exception when medicine code already exists")
    void createMedicine_DuplicateCode() {
        // Arrange
        when(medicineRepository.existsByMedicineCode("PARA-500")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateMedicineException.class, () -> {
            medicineService.createMedicine(requestDTO);
        });

        verify(medicineRepository, never()).save(any());
    }
}
