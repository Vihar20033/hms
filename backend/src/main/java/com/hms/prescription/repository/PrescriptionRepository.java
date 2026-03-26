package com.hms.prescription.repository;

import com.hms.prescription.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    // SELECT * FROM prescriptions WHERE patient_id = :patientId AND deleted = false
    List<Prescription> findByPatientId(Long patientId);

    // SELECT * FROM prescriptions WHERE patient_email = :email AND deleted = false
    List<Prescription> findByPatientEmail(String email);

    // SELECT * FROM prescriptions WHERE doctor_id = :doctorId AND deleted = false
    List<Prescription> findByDoctorId(Long doctorId);

    // SELECT * FROM prescriptions WHERE appointment_id = :appointmentId AND deleted = false
    Optional<Prescription> findByAppointmentId(Long appointmentId);

    // SELECT p.* FROM prescriptions p JOIN doctors d ON p.doctor_id = d.id WHERE d.user_id = :userId AND p.deleted = false
    List<Prescription> findByDoctorUserId(Long userId);
}
