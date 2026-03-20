package com.hms.clinical.repository;

import com.hms.clinical.entity.Vitals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VitalsRepository extends JpaRepository<Vitals, UUID> {
    Optional<Vitals> findByAppointmentId(UUID appointmentId);
    List<Vitals> findByAppointmentPatientIdOrderByCreatedAtDesc(UUID patientId);
}
