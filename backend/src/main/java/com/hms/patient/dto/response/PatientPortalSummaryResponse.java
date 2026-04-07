package com.hms.patient.dto.response;

import com.hms.appointment.dto.response.AppointmentResponseDTO;
import com.hms.billing.dto.response.BillingResponseDTO;
import com.hms.lab.dto.response.LabOrderResponse;
import com.hms.prescription.dto.response.PrescriptionResponseDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PatientPortalSummaryResponse {
    private PatientResponseDTO patient;
    private List<AppointmentResponseDTO> appointments;
    private List<LabOrderResponse> labOrders;
    private List<PrescriptionResponseDTO> prescriptions;
    private List<BillingResponseDTO> billings;
}
