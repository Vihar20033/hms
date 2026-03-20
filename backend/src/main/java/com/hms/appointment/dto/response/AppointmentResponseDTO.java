package com.hms.appointment.dto.response;

import com.hms.common.enums.AppointmentStatus;
import com.hms.common.enums.Department;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponseDTO {
    private UUID id;
    private UUID patientId;
    private String patientName;
    private UUID doctorId;
    private String doctorName;
    private Department department;
    private LocalDateTime appointmentTime;
    private AppointmentStatus status;
    private String reason;
    private String tokenNumber;
}
