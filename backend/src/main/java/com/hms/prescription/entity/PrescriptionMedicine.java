package com.hms.prescription.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
@Entity
@Audited
@Table(name = "prescription_medicines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionMedicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @Column(nullable = false)
    private String medicineName;

    private String dosage;
    private String duration;
    private Integer quantity;
    @Column(nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal unitPriceAtPrescription;

    private String instructions;
}
