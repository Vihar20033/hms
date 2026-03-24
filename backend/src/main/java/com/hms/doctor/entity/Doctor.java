package com.hms.doctor.entity;

import com.hms.common.entity.Auditable;
import com.hms.common.enums.Department;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "doctors")
@SQLDelete(sql = "UPDATE doctors SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Doctor extends Auditable {

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String specialization;

    @Column(nullable = false, unique = true)
    private String registrationNumber;

    private String contactNumber;
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
