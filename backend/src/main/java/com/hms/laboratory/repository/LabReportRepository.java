package com.hms.laboratory.repository;

import com.hms.laboratory.entity.LabReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LabReportRepository extends JpaRepository<LabReport, UUID> {
    
    // SELECT * FROM lab_reports WHERE lab_test_id = :testId AND deleted = false
    Optional<LabReport> findByLabTestId(UUID testId);

    // SELECT lr.* FROM lab_reports lr JOIN lab_tests lt ON lr.lab_test_id = lt.id JOIN patients p ON lt.patient_id = p.id WHERE p.email = :email AND lr.deleted = false
    java.util.List<LabReport> findByLabTestPatientEmail(String email);

    // SELECT lr.* FROM lab_reports lr JOIN lab_tests lt ON lr.lab_test_id = lt.id WHERE lt.patient_id = :patientId AND lr.deleted = false
    java.util.List<LabReport> findByLabTestPatientId(UUID patientId);
}
