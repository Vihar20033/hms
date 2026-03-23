package com.hms.billing.service;

import com.hms.billing.dto.request.BillingRequestDTO;
import com.hms.billing.dto.response.BillingResponseDTO;
import com.hms.common.enums.PaymentStatus;

import java.util.List;
import java.util.UUID;

public interface BillingService {
    BillingResponseDTO createBilling(BillingRequestDTO dto);

    BillingResponseDTO updateBilling(UUID id, BillingRequestDTO dto);

    BillingResponseDTO getBillingById(UUID id);

    List<BillingResponseDTO> getAllBillings();

    List<BillingResponseDTO> getBillingsByPatientId(UUID patientId);

    BillingResponseDTO updatePaymentStatus(UUID id, PaymentStatus status);

    void deleteBilling(UUID id);


    BillingResponseDTO generateBillingFromAppointment(UUID appointmentId);
    BillingResponseDTO calculatePreviewBilling(UUID appointmentId);
    com.hms.billing.entity.Billing getBillingEntityForPreview(UUID appointmentId);
}

