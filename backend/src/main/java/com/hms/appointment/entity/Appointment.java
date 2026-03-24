package com.hms.appointment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hms.common.entity.Auditable;
import com.hms.common.enums.Department;
import com.hms.patient.entity.Patient;
import com.hms.doctor.entity.Doctor;
import com.hms.common.enums.AppointmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "appointments",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_doctor_time", columnNames = {"doctor_id", "appointmentTime", "deleted"})
    },
    indexes = {
        @Index(name = "idx_doctor_time", columnList = "doctor_id, appointmentTime"),
        @Index(name = "idx_patient", columnList = "patient_id")
    }
)
@SQLDelete(sql = "UPDATE appointments SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Appointment extends Auditable {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @NotNull(message = "Patient is required")
    private Patient patient;

    @JsonIgnore // Avoid Infinite recursion
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Enumerated(EnumType.STRING)
    @Column(name = "department", nullable = false)
    @NotNull(message = "Department is required")
    private Department department;

    @Column(nullable = false)
    @NotNull(message = "Appointment time is required")
    private LocalDateTime appointmentTime;

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
}
