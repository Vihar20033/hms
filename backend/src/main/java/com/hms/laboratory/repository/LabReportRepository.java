package com.hms.laboratory.repository;

import com.hms.laboratory.entity.LabReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LabReportRepository extends JpaRepository<LabReport, UUID> {
    
    Optional<LabReport> findByLabTestId(UUID testId);

    java.util.List<LabReport> findByLabTestPatientEmail(String email);

    java.util.List<LabReport> findByLabTestPatientId(UUID patientId);
}
