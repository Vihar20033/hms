package com.hms.doctor.dto.response;

import lombok.Data;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class DoctorScheduleResponseDTO {
    private UUID id;
    private UUID doctorId;
    private String doctorName;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotDurationMinutes;
}
