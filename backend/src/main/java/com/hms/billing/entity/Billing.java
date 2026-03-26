package com.hms.billing.entity;

import com.hms.common.entity.Auditable;
import com.hms.patient.entity.Patient;
import com.hms.appointment.entity.Appointment;
import com.hms.common.enums.PaymentMethod;
import com.hms.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "billings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Billing extends Auditable {

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private LocalDateTime billingDate;

    private LocalDateTime dueDate;

    private String notes;

    private String insuranceProvider;
    private String insuranceClaimNumber;
    private java.math.BigDecimal insuranceAmount;
    private String insuranceStatus; // PENDING, APPROVED, REJECTED

    @OneToMany(mappedBy = "billing", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BillingItem> items = new ArrayList<>();

    public void addItem(BillingItem item) {
        items.add(item);
        item.setBilling(this);
    }
}
