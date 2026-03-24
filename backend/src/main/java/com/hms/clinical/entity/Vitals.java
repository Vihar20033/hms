package com.hms.clinical.entity;

import com.hms.common.entity.Auditable;
import com.hms.appointment.entity.Appointment;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "vitals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vitals extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    private BigDecimal temperature; // in Celsius
    private Integer systolicBP;
    private Integer diastolicBP;
    private Integer pulseRate;
    private Integer respiratoryRate;
    private BigDecimal spo2; // oxygen saturation %
    private BigDecimal weight; // in kg
    private BigDecimal height; // in cm
    private String notes;
}
