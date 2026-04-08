package com.hms.appointment.repository;

import com.hms.appointment.entity.Appointment;
import com.hms.common.enums.AppointmentStatus;
import com.hms.common.enums.Department;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Modifying
    @Query("UPDATE Appointment a SET a.doctor.id = :toDoctorId, a.department = :department WHERE a.doctor.id = :fromDoctorId AND a.status IN :statuses")
    int reassignDoctorBulk(
            @Param("fromDoctorId") Long fromDoctorId,
            @Param("toDoctorId") Long toDoctorId,
            @Param("department") Department department,
            @Param("statuses") Collection<AppointmentStatus> statuses
    );

    List<Appointment> findByDoctorIdAndStatusIn(Long doctorId, Collection<AppointmentStatus> statuses);

    @Override
    @EntityGraph(attributePaths = {"patient", "doctor"})
    @NonNull
    List<Appointment> findAll();

    @Override
    @EntityGraph(attributePaths = {"patient", "doctor"})
    @NonNull
    Optional<Appointment> findById(@NonNull Long id);

    long countByAppointmentTimeBetween(LocalDateTime start, LocalDateTime end);

    long countByStatusAndUpdatedAtBetween(AppointmentStatus status, LocalDateTime start, LocalDateTime end);

    long countByStatusInAndAppointmentTimeBetween(Collection<AppointmentStatus> statuses, LocalDateTime start, LocalDateTime end);

    long countByDoctorIdAndAppointmentTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);

    long countByStatus(AppointmentStatus status);

    long countByDoctorId(Long doctorId);

    long countByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);

    long countByDoctorIdAndStatusIn(Long doctorId, Collection<AppointmentStatus> statuses);

    long countByPatientIdAndStatusIn(Long patientId, Collection<AppointmentStatus> statuses);

    long countByDepartment(Department department);

    @EntityGraph(attributePaths = {"patient", "doctor"})
    List<Appointment> findByPatientIdOrderByAppointmentTimeDesc(Long patientId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentTime = :appointmentTime AND a.status IN :statuses")
    List<Appointment> findAndLockConflictingAppointments(
            @Param("doctorId") Long doctorId,
            @Param("appointmentTime") LocalDateTime appointmentTime,
            @Param("statuses") Collection<AppointmentStatus> statuses
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.appointmentTime = :appointmentTime AND a.status IN :statuses")
    List<Appointment> findAndLockPatientConflictingAppointments(
            @Param("patientId") Long patientId,
            @Param("appointmentTime") LocalDateTime appointmentTime,
            @Param("statuses") Collection<AppointmentStatus> statuses
    );

    @EntityGraph(attributePaths = {"patient", "doctor"})
    @Query("SELECT a FROM Appointment a WHERE " +
           "(:doctorUserId IS NULL OR a.doctor.userId = :doctorUserId) AND " +
           "(:patientId IS NULL OR a.patient.id = :patientId) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:startTime IS NULL OR a.appointmentTime >= :startTime) AND " +
           "(:endTime IS NULL OR a.appointmentTime <= :endTime) " +
           "ORDER BY a.appointmentTime DESC")
    List<Appointment> findAppointments(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Modifying
    @Query(value = "UPDATE appointments SET deleted = false WHERE id = :id", nativeQuery = true)
    void restore(@Param("id") Long id);
}
