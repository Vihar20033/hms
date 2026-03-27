package com.hms.appointment.repository;

import com.hms.appointment.entity.Appointment;
import com.hms.common.enums.AppointmentStatus;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long>, AppointmentRepositoryCustom, JpaSpecificationExecutor<Appointment> {

        // Eagerly fetch patient & doctor for Specification-based queries (search/filter endpoint)
        @Override
        @EntityGraph(attributePaths = {"patient", "doctor"})
        @NonNull
        Page<Appointment> findAll(Specification<Appointment> spec, @NonNull Pageable pageable);

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
}

