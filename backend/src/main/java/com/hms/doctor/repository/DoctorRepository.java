package com.hms.doctor.repository;

import com.hms.common.enums.Department;
import com.hms.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long>, JpaSpecificationExecutor<Doctor> {

    // SELECT * FROM doctors WHERE registration_number = :registrationNumber AND deleted = false
    Optional<Doctor> findByRegistrationNumber(String registrationNumber);

    // SELECT * FROM doctors WHERE department = :department AND deleted = false
    List<Doctor> findByDepartment(Department department);

    // SELECT * FROM doctors WHERE user_id = :userId AND deleted = false
    Optional<Doctor> findByUserId(Long userId);


    // SELECT EXISTS(SELECT 1 FROM doctors WHERE user_id = :userId AND deleted = false)
    boolean existsByUserId(Long userId);
}
