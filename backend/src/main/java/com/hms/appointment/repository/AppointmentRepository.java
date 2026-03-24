package com.hms.appointment.repository;

import com.hms.appointment.entity.Appointment;
import com.hms.common.enums.AppointmentStatus;
import com.hms.common.enums.Department;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

@Repository
public interface AppointmentRepository extends JpaRepository
        // JPA = Simple CRUD , Custom = Custom Slice logic , JpaSpecificationExecutor = Dynamic filtering
        <Appointment, UUID>, AppointmentRepositoryCustom, JpaSpecificationExecutor<Appointment> {

        /*
                SELECT a.*, p.*, d.*
                FROM appointments a
                LEFT JOIN patient p ON a.patient_id = p.id
                LEFT JOIN doctor d ON a.doctor_id = d.id
                WHERE a.deleted = false;
        */
        @Override
        @EntityGraph(attributePaths = {"patient", "doctor"})
        @NonNull
        List<Appointment> findAll();

        /*
                SELECT a.*, p.*, d.*
                FROM appointments a
                LEFT JOIN patient p ON a.patient_id = p.id
                LEFT JOIN doctor d ON a.doctor_id = d.id
                WHERE a.id = :id AND a.deleted = false;
        */
        @Override
        @EntityGraph(attributePaths = {"patient", "doctor"})
        @NonNull
        Optional<Appointment> findById(@NonNull UUID id);

        // SELECT a.*, p.*, d.* FROM appointments a LEFT JOIN patient p ON a.patient_id = p.id LEFT JOIN doctor d ON a.doctor_id = d.id WHERE a.patient_id = :patientId AND a.deleted = false
        @EntityGraph(attributePaths = {"patient", "doctor"})
        List<Appointment> findByPatientId(UUID patientId);

        // SELECT a.*, p.*, d.* FROM appointments a LEFT JOIN patient p ON a.patient_id = p.id LEFT JOIN doctor d ON a.doctor_id = d.id WHERE a.doctor_id = :doctorId AND a.deleted = false
        @EntityGraph(attributePaths = {"patient", "doctor"})
        List<Appointment> findByDoctorId(UUID doctorId);

        // SELECT a.*, p.*, d.* FROM appointments a LEFT JOIN patient p ON a.patient_id = p.id LEFT JOIN doctor d ON a.doctor_id = d.id WHERE a.department = :department AND a.deleted = false
        @EntityGraph(attributePaths = {"patient", "doctor"})
        List<Appointment> findByDepartment(Department department);


        // SELECT COUNT(*) FROM appointments WHERE appointment_time BETWEEN :start AND :end AND deleted = false
        long countByAppointmentTimeBetween(LocalDateTime start, LocalDateTime end);

        // SELECT COUNT(*) FROM appointments WHERE status = :status AND appointment_time BETWEEN :start AND :end AND deleted = false
        long countByStatusAndAppointmentTimeBetween(AppointmentStatus status, LocalDateTime start, LocalDateTime end);

        // SELECT COUNT(*) FROM appointments WHERE status IN (:statuses) AND appointment_time BETWEEN :start AND :end AND deleted = false
        long countByStatusInAndAppointmentTimeBetween(Collection<AppointmentStatus> statuses, LocalDateTime start, LocalDateTime end);

        // SELECT COUNT(*) FROM appointments WHERE doctor_id = :doctorId AND appointment_time BETWEEN :start AND :end AND deleted = false
        long countByDoctorIdAndAppointmentTimeBetween(UUID doctorId, LocalDateTime start, LocalDateTime end);

        // SELECT COUNT(*) FROM appointments WHERE status = :status AND deleted = false
        long countByStatus(AppointmentStatus status);

        // SELECT COUNT(*) FROM appointments WHERE doctor_id = :doctorId AND deleted = false
        long countByDoctorId(UUID doctorId);

        // SELECT COUNT(*) FROM appointments WHERE doctor_id = :doctorId AND status = :status AND deleted = false
        long countByDoctorIdAndStatus(UUID doctorId, AppointmentStatus status);
}

