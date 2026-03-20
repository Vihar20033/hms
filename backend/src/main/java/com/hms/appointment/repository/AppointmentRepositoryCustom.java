package com.hms.appointment.repository;

import com.hms.appointment.entity.Appointment;
import com.hms.common.enums.AppointmentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;


public interface AppointmentRepositoryCustom {

    Slice<Appointment> findAllAsSlice(Specification<Appointment> spec, Pageable pageable);

    List<Appointment> findConflictingAppointment(
            UUID doctorId,
            LocalDate date,
            LocalTime time,
            List<AppointmentStatus> excludedStatuses
    );

    List<Appointment> findPatientConflictingAppointment(
            UUID patientId,
            LocalDate date,
            LocalTime time,
            List<AppointmentStatus> excludedStatuses
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND a.appointmentTime = :dateTime " +
           "AND a.deleted = false " +
           "AND a.status IN :statuses")
    List<Appointment> findAndLockConflictingAppointments(
            @Param("doctorId") UUID doctorId,
            @Param("dateTime") java.time.LocalDateTime dateTime,
            @Param("statuses") List<AppointmentStatus> statuses
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId " +
           "AND a.appointmentTime = :dateTime " +
           "AND a.deleted = false " +
           "AND a.status IN :statuses")
    List<Appointment> findAndLockPatientConflictingAppointments(
            @Param("patientId") UUID patientId,
            @Param("dateTime") java.time.LocalDateTime dateTime,
            @Param("statuses") List<AppointmentStatus> statuses
    );
}

