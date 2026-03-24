package com.hms.appointment.dto.request;

import com.hms.common.enums.Department;
import jakarta.validation.constraints.*;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Request DTO for creating and updating appointments.
 * Includes comprehensive validation constraints for production use.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentRequestDTO {

    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    @NotNull(message = "Doctor ID is required")
    private UUID doctorId;

    @NotNull(message = "Department is required")
    private Department department;

    @NotNull(message = "Appointment date is required")
    @FutureOrPresent(message = "Appointment date must be today or in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate appointmentDate;

    @NotNull(message = "Appointment time is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime appointmentTime;

    @NotBlank(message = "Reason is required")
    @Size(min = 3, max = 500, message = "Reason must be between 3 and 500 characters")
    private String reason;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

    private boolean isEmergency;

    /**
     * Validates that the appointment time is within clinic hours.
     * Clinic hours: 8:00 AM to 8:00 PM
     * Emergency appointments override this restriction.
     */
    @AssertTrue(message = "Appointment time must be between 08:00 and 20:00")
    public boolean isWithinClinicHours() {
        if (isEmergency) {
            return true;
        }
        if (appointmentTime == null) {
            return true;
        }
        LocalTime clinicOpen = LocalTime.of(8, 0);
        LocalTime clinicClose = LocalTime.of(20, 0);
        return !appointmentTime.isBefore(clinicOpen) && !appointmentTime.isAfter(clinicClose);
    }
}
