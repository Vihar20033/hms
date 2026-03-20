package com.hms.patient.entity;

import com.hms.common.entity.Auditable;
import com.hms.common.enums.BloodGroup;
import com.hms.common.enums.UrgencyLevel;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;


@Entity
@Table(
        name = "patients",
        indexes = {
                @Index(name = "idx_patient_name", columnList = "name"),
                @Index(name = "idx_patient_blood_group", columnList = "blood_group"),
                @Index(name = "idx_patient_urgency", columnList = "urgency_level"),
                @Index(name = "idx_patient_contact", columnList = "contactNumber", unique = true)
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_patient_contact", columnNames = {"contactNumber"})
        }
)
@SQLDelete(sql = "UPDATE patients SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
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
