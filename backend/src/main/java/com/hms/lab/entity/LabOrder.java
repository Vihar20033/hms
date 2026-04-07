package com.hms.lab.entity;

import com.hms.appointment.entity.Appointment;
import com.hms.common.entity.Auditable;
import com.hms.common.enums.LabOrderStatus;
import com.hms.patient.entity.Patient;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "lab_orders",
        indexes = {
                @Index(name = "idx_lab_patient", columnList = "patient_id"),
                @Index(name = "idx_lab_appointment", columnList = "appointment_id"),
                @Index(name = "idx_lab_status", columnList = "status"),
                @Index(name = "idx_lab_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class LabOrder extends Auditable {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Column(nullable = false, length = 150)
    private String testName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private LabOrderStatus status = LabOrderStatus.ORDERED;

    @Column(columnDefinition = "TEXT")
    private String resultSummary;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
