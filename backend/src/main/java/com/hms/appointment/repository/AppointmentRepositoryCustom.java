package com.hms.appointment.repository;

import com.hms.appointment.entity.Appointment;
import com.hms.common.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;

import java.time.LocalDateTime;

public interface AppointmentRepositoryCustom {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND a.appointmentTime = :dateTime " +
           "AND a.status IN :statuses")
    List<Appointment> findAndLockConflictingAppointments(
            @Param("doctorId") Long doctorId,
            @Param("dateTime") LocalDateTime dateTime,
            @Param("statuses") List<AppointmentStatus> statuses
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId " +
           "AND a.appointmentTime = :dateTime " +
           "AND a.status IN :statuses")
    List<Appointment> findAndLockPatientConflictingAppointments(
            @Param("patientId") Long patientId,
            @Param("dateTime") LocalDateTime dateTime,
            @Param("statuses") List<AppointmentStatus> statuses
    );
}
