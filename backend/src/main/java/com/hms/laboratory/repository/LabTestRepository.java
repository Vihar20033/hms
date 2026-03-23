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
    
    Optional<LabTest> findByTestCode(String testCode);

    List<LabTest> findByPatientId(UUID patientId);

    List<LabTest> findByPatientEmail(String email);

    boolean existsByTestCode(String testCode);

    List<LabTest> findByAppointmentId(UUID appointmentId);

    List<LabTest> findByRequestedByUserId(UUID userId);
}
