package com.hms.doctor.entity;

import com.hms.common.entity.Auditable;
import com.hms.common.enums.Department;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;


@Entity
@Table(
        name = "doctors",
        indexes = {
                @Index(name = "idx_doctor_user_id", columnList = "userId"),
                @Index(name = "idx_doctor_department", columnList = "department"),
                @Index(name = "idx_doctor_registration", columnList = "registrationNumber")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Doctor extends Auditable {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String specialization;

    @Column(nullable = false, unique = true)
    private String registrationNumber;
    private String email;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Enumerated(EnumType.STRING)
    private Department department;

    private String qualification;
    private Integer experienceYears;
    private String licenseNumber;
    private BigDecimal consultationFee;
    private Boolean isAvailable;
    private String phoneNumber;
    private String designation;
}
