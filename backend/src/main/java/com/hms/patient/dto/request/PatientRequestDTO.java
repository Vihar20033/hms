package com.hms.patient.dto.request;

import com.hms.common.enums.BloodGroup;
import com.hms.common.enums.Gender;
import com.hms.common.enums.UrgencyLevel;
import com.hms.common.validation.ValidationPatterns;
import java.time.LocalDate;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PatientRequestDTO {

    @NotBlank
    @Size(min = 2, max = 200)
    @Pattern(regexp = ValidationPatterns.FULL_NAME, message = "Name may contain letters, spaces, apostrophes, dots, and hyphens only")
    private String name;

    @Email
    @Size(max = 100)
    private String email;

    @Min(0)
    @Max(120)
    private Integer age;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;

    @NotNull(message = "Gender is required")
    private Gender gender;


    @NotNull
    private BloodGroup bloodGroup;

    @NotBlank
    @Size(max = 2000)
    private String prescription;

    @NotBlank
    @Size(max = 500)
    private String dose;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal fees;

    @NotBlank
    @Pattern(regexp = ValidationPatterns.PHONE, message = "Contact number must contain 10 to 15 digits and may start with +")
    private String contactNumber;

    @NotNull
    private UrgencyLevel urgencyLevel;
}
