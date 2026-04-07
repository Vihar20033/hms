package com.hms.appointment.dto.response;

import com.hms.common.enums.AppointmentStatus;
import com.hms.common.enums.Department;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponseDTO {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private Department department;
    private LocalDateTime appointmentTime;
    private AppointmentStatus status;
    private String reason;
    private String tokenNumber;
    private boolean hasPrescription;

    private Long version;
}
