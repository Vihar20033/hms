package com.hms.patient.repository;

import com.hms.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByContactNumber(String contactNumber);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
