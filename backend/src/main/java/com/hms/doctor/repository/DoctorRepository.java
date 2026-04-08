package com.hms.doctor.repository;

import com.hms.common.enums.Department;
import com.hms.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findByDepartment(Department department);

    Optional<Doctor> findByUserId(Long userId);

    /**
     * Restores a soft-deleted doctor record.
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE doctors SET deleted = false WHERE id = :id", nativeQuery = true)
    int restore(@Param("id") Long id);
}
