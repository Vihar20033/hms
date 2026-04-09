package com.hms.appointment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

@Entity
@Immutable
@Table(name = "v_daily_appointment_schedule")
@Getter
public class DailyAppointmentView {

    @Id
    private Long appointmentId;
    
    private Instant appointmentTime;
    
    private String status;
    
    private String department;
    
    private String patientName;
    
    private String contactNumber;
    
    private String urgencyLevel;
}
