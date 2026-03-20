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
            UUID doctorId,
            LocalDate date,
            LocalTime time,
            List<AppointmentStatus> excludedStatuses) {
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Appointment> cq = cb.createQuery(Appointment.class);
        Root<Appointment> root = cq.from(Appointment.class);

        List<Predicate> predicates = new ArrayList<>();
        
        // Not deleted
        predicates.add(cb.isFalse(root.get("deleted")));
        
        // Doctor matches
        predicates.add(cb.equal(root.get("doctor").get("id"), doctorId));
        
        // Date matches
        predicates.add(cb.equal(root.get("appointmentDate"), date));
        
        // Time matches
        predicates.add(cb.equal(root.get("appointmentTime"), time));
        
        // Status not in excluded
        predicates.add(root.get("status").in(excludedStatuses).not());

        cq.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<Appointment> findPatientConflictingAppointment(
            UUID patientId,
            LocalDate date,
            LocalTime time,
            List<AppointmentStatus> excludedStatuses) {
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Appointment> cq = cb.createQuery(Appointment.class);
        Root<Appointment> root = cq.from(Appointment.class);

        List<Predicate> predicates = new ArrayList<>();
        
        // Not deleted
        predicates.add(cb.isFalse(root.get("deleted")));
        
        // Patient matches
        predicates.add(cb.equal(root.get("patient").get("id"), patientId));
        
        // Date matches
        predicates.add(cb.equal(root.get("appointmentDate"), date));
        
        // Time matches
        predicates.add(cb.equal(root.get("appointmentTime"), time));
        
        // Status not in excluded
        predicates.add(root.get("status").in(excludedStatuses).not());

        cq.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<Appointment> findAndLockConflictingAppointments(
            UUID doctorId,
            java.time.LocalDateTime dateTime,
            List<AppointmentStatus> statuses) {
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Appointment> cq = cb.createQuery(Appointment.class);
        Root<Appointment> root = cq.from(Appointment.class);

        List<Predicate> predicates = new ArrayList<>();
        
        // Not deleted
        predicates.add(cb.isFalse(root.get("deleted")));
        
        // Doctor matches
        predicates.add(cb.equal(root.get("doctor").get("id"), doctorId));
        
        // Date-Time matches
        predicates.add(cb.equal(root.get("appointmentTime"), dateTime));
        
        // Status in active statuses
        predicates.add(root.get("status").in(statuses));

        cq.where(predicates.toArray(new Predicate[0]));

        TypedQuery<Appointment> query = entityManager.createQuery(cq);
        // The @Lock annotation in the interface handles pessimistic locking
        return query.getResultList();
    }

    @Override
    public List<Appointment> findAndLockPatientConflictingAppointments(
            UUID patientId,
            java.time.LocalDateTime dateTime,
            List<AppointmentStatus> statuses) {
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Appointment> cq = cb.createQuery(Appointment.class);
        Root<Appointment> root = cq.from(Appointment.class);

        List<Predicate> predicates = new ArrayList<>();
        
        // Not deleted
        predicates.add(cb.isFalse(root.get("deleted")));
        
        // Patient matches
        predicates.add(cb.equal(root.get("patient").get("id"), patientId));
        
        // Date-Time matches
        predicates.add(cb.equal(root.get("appointmentTime"), dateTime));
        
        
        // Status in active statuses
        predicates.add(root.get("status").in(statuses));

        cq.where(predicates.toArray(new Predicate[0]));

        TypedQuery<Appointment> query = entityManager.createQuery(cq);
        // The @Lock annotation in the interface handles pessimistic locking
        return query.getResultList();
    }
}

