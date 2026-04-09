package com.hms.patient.repository;

import com.hms.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long>, JpaSpecificationExecutor<Patient> {

    boolean existsByContactNumber(String contactNumber);

    Optional<Patient> findByEmail(String email);

    long countByCreatedAtBetween(Instant start, Instant end);

    @Modifying
    @Transactional
    @Query(value = "UPDATE patients SET deleted = false WHERE id = :id", nativeQuery = true)
    int restore(@Param("id") Long id);


    @Query(value = "SELECT * FROM patients WHERE id = :id", nativeQuery = true)
    Optional<Patient> findByIdIncludingDeleted(@Param("id") Long id);
}
