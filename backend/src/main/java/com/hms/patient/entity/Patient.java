package com.hms.patient.entity;

import com.hms.common.entity.Auditable;
import com.hms.common.enums.BloodGroup;
import com.hms.common.enums.UrgencyLevel;
import jakarta.persistence.*;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;


@Entity
@Table(
        name = "patients",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_patient_contact", columnNames = {"contactNumber"}),
                @UniqueConstraint(name = "uk_patient_email", columnNames = {"email"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Patient extends Auditable {

    @Column(nullable = false, length = 200)
    private String name;

    @Min(0)
    @Max(150)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", length = 20)
    private BloodGroup bloodGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgency_level", length = 20)
    private UrgencyLevel urgencyLevel;

    @Column(columnDefinition = "TEXT")
    private String prescription;

    @Column(length = 500)
    private String dose;

    @Column(nullable = false, precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Fees must be positive")
    private BigDecimal fees;

    @Column(nullable = false, unique = true, length = 15)
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Invalid contact number")
    private String contactNumber;

    @Email
    @Column(length = 100)
    private String email;

    @Column(length = 500)
    private String address;

}
