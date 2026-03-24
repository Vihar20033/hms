package com.hms.appointment.repository;

import com.hms.appointment.entity.Appointment;
import com.hms.common.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.UUID;


public interface AppointmentRepositoryCustom {

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

