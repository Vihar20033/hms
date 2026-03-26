package com.hms.appointment.repository;

import com.hms.appointment.entity.Appointment;
import com.hms.common.enums.AppointmentStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Repository
@RequiredArgsConstructor
public class AppointmentRepositoryImpl implements AppointmentRepositoryCustom {

    private final EntityManager entityManager;


    // Method checks doctor already booked at this time
    @Override
    public List<Appointment> findAndLockConflictingAppointments(
            Long doctorId, LocalDateTime dateTime, List<AppointmentStatus> statuses) {
        return findAndLockConflictByEntity("doctor", doctorId, dateTime, statuses);
    }

    // Method checks patient already booked that time
    @Override
    public List<Appointment> findAndLockPatientConflictingAppointments(
            Long patientId, LocalDateTime dateTime, List<AppointmentStatus> statuses) {
        return findAndLockConflictByEntity("patient", patientId, dateTime, statuses);
    }


    private List<Appointment> findAndLockConflictByEntity(
            String relationName, Long entityId, LocalDateTime dateTime, List<AppointmentStatus> statuses) {
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Appointment> cq = cb.createQuery(Appointment.class);
        Root<Appointment> root = cq.from(Appointment.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get(relationName).get("id"), entityId));
        predicates.add(cb.equal(root.get("appointmentTime"), dateTime));
        predicates.add(root.get("status").in(statuses));

        cq.where(predicates.toArray(new Predicate[0]));
        
        return entityManager.createQuery(cq)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();
    }
}
