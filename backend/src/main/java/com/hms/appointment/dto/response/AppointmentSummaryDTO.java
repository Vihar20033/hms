package com.hms.appointment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentSummaryDTO {
    private long total;
    private long scheduled;
    private long checkedIn;
    private long inConsultation;
    private long completed;
    private long cancelled;
}
