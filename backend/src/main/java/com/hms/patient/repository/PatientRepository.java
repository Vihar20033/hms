package com.hms.patient.repository;

import com.hms.patient.entity.Patient;
import org.springframework.data.jpa.repository.*;

import java.time.LocalDateTime;
import java.util.Optional;


public interface PatientRepository
        extends JpaRepository
        <Patient, Long>, JpaSpecificationExecutor<Patient>, PatientRepositoryCustom
{
    // SELECT EXISTS(SELECT 1 FROM patients WHERE contact_number = :contactNumber AND deleted = false)
    boolean existsByContactNumber(String contactNumber);

    // SELECT COUNT(*) FROM patients WHERE created_at BETWEEN :start AND :end AND deleted = false
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
