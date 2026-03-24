package com.hms.laboratory.repository;

import com.hms.laboratory.entity.LabTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LabTestRepository extends JpaRepository<LabTest, UUID>, JpaSpecificationExecutor<LabTest> {
    
    // SELECT * FROM lab_tests WHERE test_code = :testCode AND deleted = false
    Optional<LabTest> findByTestCode(String testCode);

    // SELECT * FROM lab_tests WHERE patient_id = :patientId AND deleted = false
    List<LabTest> findByPatientId(UUID patientId);

    // SELECT * FROM lab_tests WHERE patient_email = :email AND deleted = false
    List<LabTest> findByPatientEmail(String email);

    // SELECT EXISTS(SELECT 1 FROM lab_tests WHERE test_code = :testCode AND deleted = false)
    boolean existsByTestCode(String testCode);

    // SELECT * FROM lab_tests WHERE appointment_id = :appointmentId AND deleted = false
    List<LabTest> findByAppointmentId(UUID appointmentId);

    // SELECT * FROM lab_tests WHERE requested_by_user_id = :userId AND deleted = false
    List<LabTest> findByRequestedByUserId(java.util.UUID userId);

    // SELECT COUNT(*) FROM lab_tests WHERE status IN (:statuses) AND deleted = false
    long countByStatusIn(java.util.Collection<com.hms.common.enums.TestStatus> statuses);
}
