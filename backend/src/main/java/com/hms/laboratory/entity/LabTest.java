package com.hms.laboratory.entity;

import com.hms.common.entity.Auditable;
import com.hms.patient.entity.Patient;
import com.hms.doctor.entity.Doctor;
import com.hms.appointment.entity.Appointment;
import com.hms.common.enums.TestStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lab_tests")
@SQLDelete(sql = "UPDATE lab_tests SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class LabTest extends Auditable {

    @Column(nullable = false)
    private String testName;

    @Column(nullable = false, unique = true)
    private String testCode;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestStatus status;

    private LocalDateTime requestedDate;
    private LocalDateTime completedDate;

    @OneToOne(mappedBy = "labTest", cascade = CascadeType.ALL)
    private LabReport report;
}
