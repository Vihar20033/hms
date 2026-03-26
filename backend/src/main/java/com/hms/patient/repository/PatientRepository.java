package com.hms.patient.repository;

import com.hms.patient.entity.Patient;
import org.springframework.data.jpa.repository.*;

import java.time.LocalDateTime;


public interface PatientRepository
        extends JpaRepository
        <Patient, Long>, JpaSpecificationExecutor<Patient>, PatientRepositoryCustom
{
    boolean existsByContactNumber(String contactNumber);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
