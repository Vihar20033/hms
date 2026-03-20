package com.hms.appointment.entity;

import com.hms.common.entity.Auditable;
import com.hms.patient.entity.Patient;
import com.hms.doctor.entity.Doctor;
import com.hms.common.enums.AppointmentStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"doctor_id", "appointmentTime", "deleted"})
})
@SQLDelete(sql = "UPDATE appointments SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Appointment extends Auditable {

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "patient_id", nullable = false)
        private Patient patient;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "doctor_id")
        private Doctor doctor;

        @Enumerated(EnumType.STRING)
        @Column(name = "department", nullable = false)
        private com.hms.common.enums.Department department;

        @Column(nullable = false)
        private LocalDateTime appointmentTime;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private AppointmentStatus status;

        private String reason;

        @Column(columnDefinition = "TEXT")
        private String notes;

        private String tokenNumber;

        @Builder.Default
        private boolean isEmergency = false;
}
