package com.hms.appointment.repository;

import com.hms.appointment.entity.Appointment;
import com.hms.common.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID>, AppointmentRepositoryCustom {
        @Override
        @EntityGraph(attributePaths = {"patient", "doctor"})
        List<Appointment> findAll();

        @Override
        @EntityGraph(attributePaths = {"patient", "doctor"})
        Optional<Appointment> findById(UUID id);

        @EntityGraph(attributePaths = {"patient", "doctor"})
        List<Appointment> findByPatientId(UUID patientId);

        @EntityGraph(attributePaths = {"patient", "doctor"})
        List<Appointment> findByDoctorId(UUID doctorId);

        @EntityGraph(attributePaths = {"patient", "doctor"})
        List<Appointment> findByDepartment(com.hms.common.enums.Department department);

        @EntityGraph(attributePaths = {"patient", "doctor"})
        List<Appointment> findByStatus(AppointmentStatus status);

        long countByAppointmentTimeBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);

        long countByStatusAndAppointmentTimeBetween(AppointmentStatus status, java.time.LocalDateTime start, java.time.LocalDateTime end);

        long countByStatusInAndAppointmentTimeBetween(java.util.Collection<AppointmentStatus> statuses, java.time.LocalDateTime start, java.time.LocalDateTime end);

        boolean existsByDoctorIdAndAppointmentTime(UUID doctorId, java.time.LocalDateTime appointmentTime);

        long countByDoctorIdAndAppointmentTimeBetween(UUID doctorId, java.time.LocalDateTime start, java.time.LocalDateTime end);
}

