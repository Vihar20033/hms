package com.hms.appointment.entity;

import com.hms.common.entity.Auditable;
import com.hms.common.enums.Department;
import com.hms.patient.entity.Patient;
import com.hms.doctor.entity.Doctor;
import com.hms.common.enums.AppointmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;

import java.time.Instant;

@Entity
@Audited
@Table(
    name = "appointments",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_doctor_time", columnNames = {"doctor_id", "appointmentTime"})
    },
    indexes = {
        @Index(name = "idx_appointment_patient", columnList = "patient_id"),
        @Index(name = "idx_appointment_doctor", columnList = "doctor_id"),
        @Index(name = "idx_appointment_status_time", columnList = "status, appointmentTime"),
        @Index(name = "idx_appointment_department", columnList = "department")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Appointment extends Auditable {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    @NotNull(message = "Patient is required")
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Enumerated(EnumType.STRING)
    @Column(name = "department", nullable = false)
    @NotNull(message = "Department is required")
    private Department department;

    @Column(nullable = false)
    @NotNull(message = "Appointment time is required")
    private Instant appointmentTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    private String reason;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private String tokenNumber;

    @Builder.Default
    private boolean isEmergency = false;

    @Builder.Default
    private Integer severityScore = 0; // 1 (Low) to 10 (Critical)
}
