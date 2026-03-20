package com.hms.prescription.repository;

import com.hms.prescription.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {
    List<Prescription> findByPatientId(UUID patientId);

    List<Prescription> findByDoctorId(UUID doctorId);

    Optional<Prescription> findByAppointmentId(UUID appointmentId);
}
