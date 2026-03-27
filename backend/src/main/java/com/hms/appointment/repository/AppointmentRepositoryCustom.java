package com.hms.appointment.repository;

import com.hms.appointment.entity.Appointment;
import com.hms.common.enums.AppointmentStatus;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepositoryCustom {
    List<Appointment> findAndLockConflictingAppointments(
            Long doctorId,
            LocalDateTime dateTime,
            List<AppointmentStatus> statuses
    );

    List<Appointment> findAndLockPatientConflictingAppointments(
            Long patientId,
            LocalDateTime dateTime,
            List<AppointmentStatus> statuses
    );
}
