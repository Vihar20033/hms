package com.hms.doctor.repository;

import com.hms.common.enums.Department;
import com.hms.doctor.entity.Doctor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

        @Query("SELECT d FROM Doctor d " +
            "WHERE LOWER(d.firstName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(d.lastName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(d.registrationNumber) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(d.licenseNumber) LIKE LOWER(CONCAT('%', :query, '%')) " +
                "OR LOWER(d.email) LIKE LOWER(CONCAT('%', :query, '%'))")
        Slice<Doctor> searchDoctors(@Param("query") String query, Pageable pageable);

    /**
     * Restores a soft-deleted doctor record.
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE doctors SET deleted = false WHERE id = :id", nativeQuery = true)
    int restore(@Param("id") Long id);
}
