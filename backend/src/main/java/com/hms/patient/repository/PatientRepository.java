package com.hms.patient.repository;

import com.hms.patient.entity.Patient;
import org.springframework.data.jpa.repository.*;
import java.util.UUID;

public interface PatientRepository
        extends JpaRepository<Patient, UUID>,
        JpaSpecificationExecutor<Patient>,
        PatientRepositoryCustom {

    boolean existsByContactNumber(String contactNumber);

    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}