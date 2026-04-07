package com.hms.prescription.entity;

import com.hms.common.entity.Auditable;
import com.hms.patient.entity.Patient;
import com.hms.doctor.entity.Doctor;
import com.hms.appointment.entity.Appointment;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "prescriptions",
        indexes = {
                @Index(name = "idx_prescription_patient", columnList = "patient_id"),
                @Index(name = "idx_prescription_doctor", columnList = "doctor_id"),
                @Index(name = "idx_prescription_appointment", columnList = "appointment_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Prescription extends Auditable {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PrescriptionMedicine> medicines = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String advice;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(length = 500)
    private String reportUrl;

    public void addMedicine(PrescriptionMedicine medicine) {
        medicines.add(medicine);
        medicine.setPrescription(this);
    }
}
