package com.hms.patient.repository;

import com.hms.patient.entity.Patient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom implementation returning Slice without COUNT query
 */
@Repository
@RequiredArgsConstructor
public class PatientRepositoryImpl implements PatientRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public Slice<Patient> findAllAsSlice(Specification<Patient> spec, Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Patient> cq = cb.createQuery(Patient.class);
        Root<Patient> root = cq.from(Patient.class);

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

        TypedQuery<Patient> query = entityManager.createQuery(cq);

        query.setFirstResult((int) pageable.getOffset());

        int pageSize = pageable.getPageSize();

        query.setMaxResults(pageSize + 1);

        List<Patient> results = query.getResultList();

        boolean hasNext = results.size() > pageSize;

        List<Patient> content = hasNext
                ? results.subList(0, pageSize)
                : results;

        return new SliceImpl<>(content, pageable, hasNext);
    }
}