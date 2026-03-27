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
    List<Doctor> findByDepartment(Department department);

    Optional<Doctor> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
