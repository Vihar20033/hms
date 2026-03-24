package com.hms.clinical.repository;

import com.hms.clinical.entity.Vitals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VitalsRepository extends JpaRepository<Vitals, UUID> {
    // SELECT * FROM vitals WHERE appointment_id = :appointmentId AND deleted = false
    Optional<Vitals> findByAppointmentId(UUID appointmentId);

    // SELECT v.* FROM vitals v JOIN appointments a ON v.appointment_id = a.id WHERE a.patient_id = :patientId AND v.deleted = false ORDER BY v.created_at DESC
    List<Vitals> findByAppointmentPatientIdOrderByCreatedAtDesc(UUID patientId);
}
