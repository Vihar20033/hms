package com.hms.patient.repository;

import com.hms.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByContactNumber(String contactNumber);

    Optional<Patient> findByEmail(String email);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
