package com.hms.laboratory.service.impl;

import com.hms.common.audit.AuditLogService;
import com.hms.laboratory.dto.request.LabTestRequestDTO;
import com.hms.laboratory.dto.response.LabTestResponseDTO;
import com.hms.laboratory.entity.LabTest;
import com.hms.laboratory.mapper.LabMapper;
import com.hms.laboratory.repository.LabTestRepository;
import com.hms.common.enums.TestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabServiceImplTest {

    @Mock
    private LabTestRepository labTestRepository;

    @Mock
    private LabMapper labMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private LabServiceImpl labService;

    private LabTestRequestDTO requestDTO;
    private LabTest mockTest;

    @BeforeEach
    void setUp() {
        requestDTO = new LabTestRequestDTO();
        requestDTO.setTestName("CBC Test");
        requestDTO.setCategory("Hematology");
        requestDTO.setPatientId(UUID.randomUUID());

        mockTest = new LabTest();
        mockTest.setId(UUID.randomUUID());
        mockTest.setTestName("CBC Test");
        mockTest.setStatus(TestStatus.PENDING);
    }

    @Test
    @DisplayName("Should update test status correctly")
    void recordResults_Success() {
        when(labTestRepository.findById(any())).thenReturn(Optional.of(mockTest));
        when(labTestRepository.save(any())).thenReturn(mockTest);
        when(labMapper.toDto(any())).thenReturn(new LabTestResponseDTO());

        LabTestResponseDTO result = labService.updateTestStatus(mockTest.getId(), TestStatus.COMPLETED);

        assertNotNull(result);
        assertEquals(TestStatus.COMPLETED, mockTest.getStatus());
        verify(labTestRepository).save(any());
        verify(auditLogService).log(any(), eq("LAB_TEST_STATUS_UPDATE"), eq("LabTest"), any(), any());
    }

    @Test
    @DisplayName("Should fail when updating status for non-existent test")
    void recordResults_NotFound() {
        when(labTestRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> labService.updateTestStatus(UUID.randomUUID(), TestStatus.COMPLETED));
        verify(labTestRepository, never()).save(any());
    }
}
