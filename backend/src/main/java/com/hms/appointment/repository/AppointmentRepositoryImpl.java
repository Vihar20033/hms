package com.hms.appointment.repository;

import com.hms.appointment.entity.Appointment;
import com.hms.common.enums.AppointmentStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AppointmentRepositoryImpl implements AppointmentRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public Slice<Appointment> findAllAsSlice(Specification<Appointment> spec, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Appointment> cq = cb.createQuery(Appointment.class);
        Root<Appointment> root = cq.from(Appointment.class);

        Predicate predicate = spec.toPredicate(root, cq, cb);
        if (predicate != null) {
            cq.where(predicate);
        }

        List<Order> orders = new ArrayList<>();
        pageable.getSort().forEach(order ->
                orders.add(order.isAscending()
                        ? cb.asc(root.get(order.getProperty()))
                        : cb.desc(root.get(order.getProperty()))));
        if (!orders.isEmpty()) {
            cq.orderBy(orders);
        }

        TypedQuery<Appointment> query = entityManager.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        int pageSize = pageable.getPageSize();
        query.setMaxResults(pageSize + 1);

        List<Appointment> results = query.getResultList();
        boolean hasNext = results.size() > pageSize;
        List<Appointment> content = hasNext ? results.subList(0, pageSize) : results;

        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public List<Appointment> findConflictingAppointment(
            UUID doctorId, LocalDate date, LocalTime time, List<AppointmentStatus> excludedStatuses) {
        return findConflictByEntity("doctor", doctorId, date, time, excludedStatuses);
    }

    @Override
    public List<Appointment> findPatientConflictingAppointment(
            UUID patientId, LocalDate date, LocalTime time, List<AppointmentStatus> excludedStatuses) {
        return findConflictByEntity("patient", patientId, date, time, excludedStatuses);
    }

    @Override
    public List<Appointment> findAndLockConflictingAppointments(
            UUID doctorId, LocalDateTime dateTime, List<AppointmentStatus> statuses) {
        return findAndLockConflictByEntity("doctor", doctorId, dateTime, statuses);
    }

    @Override
    public List<Appointment> findAndLockPatientConflictingAppointments(
            UUID patientId, LocalDateTime dateTime, List<AppointmentStatus> statuses) {
        return findAndLockConflictByEntity("patient", patientId, dateTime, statuses);
    }

    private List<Appointment> findConflictByEntity(
            String relationName, UUID entityId, LocalDate date, LocalTime time, List<AppointmentStatus> excludedStatuses) {
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Appointment> cq = cb.createQuery(Appointment.class);
        Root<Appointment> root = cq.from(Appointment.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isFalse(root.get("deleted")));
        predicates.add(cb.equal(root.get(relationName).get("id"), entityId));
        predicates.add(cb.equal(root.get("appointmentDate"), date));
        predicates.add(cb.equal(root.get("appointmentTime"), time));
        predicates.add(root.get("status").in(excludedStatuses).not());

        cq.where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(cq).getResultList();
    }

    private List<Appointment> findAndLockConflictByEntity(
            String relationName, UUID entityId, LocalDateTime dateTime, List<AppointmentStatus> statuses) {
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Appointment> cq = cb.createQuery(Appointment.class);
        Root<Appointment> root = cq.from(Appointment.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isFalse(root.get("deleted")));
        predicates.add(cb.equal(root.get(relationName).get("id"), entityId));
        predicates.add(cb.equal(root.get("appointmentTime"), dateTime));
        predicates.add(root.get("status").in(statuses));

        cq.where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(cq).getResultList();
    }
}
