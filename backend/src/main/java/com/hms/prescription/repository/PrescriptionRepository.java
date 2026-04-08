package com.hms.prescription.repository;

import com.hms.prescription.entity.Prescription;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByPatientId(Long patientId);
    
    Slice<Prescription> findByPatientId(Long patientId, Pageable pageable);

    Optional<Prescription> findByAppointmentId(Long appointmentId);

    List<Prescription> findByDoctorUserId(Long userId);
    
    Slice<Prescription> findByDoctorUserId(Long userId, Pageable pageable);

    @Modifying
    @Query(value = "UPDATE prescriptions SET deleted = false WHERE id = :id", nativeQuery = true)
    void restore(@Param("id") Long id);
}
