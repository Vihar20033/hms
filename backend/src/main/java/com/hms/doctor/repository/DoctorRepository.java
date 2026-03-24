package com.hms.doctor.repository;

import com.hms.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    // SELECT * FROM doctors WHERE registration_number = :registrationNumber AND deleted = false
    Optional<Doctor> findByRegistrationNumber(String registrationNumber);

    // SELECT * FROM doctors WHERE department = :department AND deleted = false
    List<Doctor> findByDepartment(com.hms.common.enums.Department department);

    // SELECT * FROM doctors WHERE user_id = :userId AND deleted = false
    Optional<Doctor> findByUserId(UUID userId);

    // SELECT * FROM doctors WHERE specialization LIKE %:specialization% AND deleted = false
    List<Doctor> findBySpecializationContaining(String specialization);

    // SELECT EXISTS(SELECT 1 FROM doctors WHERE user_id = :userId AND deleted = false)
    boolean existsByUserId(UUID userId);
}
