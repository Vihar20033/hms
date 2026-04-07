package com.hms.billing.service;

import com.hms.billing.dto.request.BillingRequestDTO;
import com.hms.billing.dto.response.BillingResponseDTO;
import com.hms.common.enums.PaymentStatus;
import java.util.List;

public interface BillingService {
    BillingResponseDTO createBilling(BillingRequestDTO dto);



    BillingResponseDTO getBillingById(Long id);

    List<BillingResponseDTO> getAllBillings();

    List<BillingResponseDTO> getBillingsByPatientId(Long patientId);

    List<BillingResponseDTO> getCurrentPatientBillings();

    BillingResponseDTO updatePaymentStatus(Long id, PaymentStatus status);

    BillingResponseDTO payCurrentPatientBill(Long id);

    void deleteBilling(Long id);

    BillingResponseDTO generateBillingFromAppointment(Long appointmentId);
    BillingResponseDTO calculatePreviewBilling(Long appointmentId);
}
