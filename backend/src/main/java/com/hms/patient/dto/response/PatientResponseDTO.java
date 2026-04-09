package com.hms.patient.dto.response;

import com.hms.common.enums.BloodGroup;
import com.hms.common.enums.Gender;
import com.hms.common.enums.UrgencyLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponseDTO {

    private Long id;
    private String name;
    private int age;
    private LocalDate dob;
    private Gender gender;

    private BloodGroup bloodGroup;
    private String prescription;
    private String dose;
    private BigDecimal fees;
    private String contactNumber;
    private UrgencyLevel urgencyLevel;
    private Instant createdAt;
}
