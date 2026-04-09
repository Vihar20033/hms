package com.hms.appointment.dto.request;

import com.hms.common.enums.Department;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentRequestDTO {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Department is required")
    private Department department;

    @NotNull(message = "Appointment time is required")
    @FutureOrPresent(message = "Appointment time must be in the future")
    private java.time.Instant appointmentTime;

    @NotBlank(message = "Reason is required")
    @Size(min = 3, max = 500, message = "Reason must be between 3 and 500 characters")
    private String reason;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

    private boolean isEmergency;

    private Long version;

}
