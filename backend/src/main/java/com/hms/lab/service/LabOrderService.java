package com.hms.lab.service;

import com.hms.common.enums.LabOrderStatus;
import com.hms.lab.dto.request.LabOrderRequest;
import com.hms.lab.dto.request.LabResultRequest;
import com.hms.lab.dto.response.LabOrderResponse;

import java.util.List;

public interface LabOrderService {
    LabOrderResponse create(LabOrderRequest request);
    List<LabOrderResponse> getAll(LabOrderStatus status);
    List<LabOrderResponse> getByPatient(Long patientId);
    List<LabOrderResponse> getCurrentPatientOrders();
    LabOrderResponse updateStatus(Long id, LabOrderStatus status);
    LabOrderResponse publishResult(Long id, LabResultRequest request);
}
