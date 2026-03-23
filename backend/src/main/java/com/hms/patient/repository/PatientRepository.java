package com.hms.patient.repository;

import com.hms.patient.entity.Patient;
import org.springframework.data.jpa.repository.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository
        extends JpaRepository
        <Patient, UUID>, JpaSpecificationExecutor<Patient>, PatientRepositoryCustom
{
    boolean existsByContactNumber(String contactNumber);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}