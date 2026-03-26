package com.hms.appointment.dto.request;

import com.hms.common.enums.AppointmentStatus;
import com.hms.common.enums.Department;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class AppointmentSearchCriteria {
    private String query;
    private Long doctorId;
    private Long patientId;
    private AppointmentStatus status;
    private Department department;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime start;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime end;
    
    private Boolean isEmergency;
}
