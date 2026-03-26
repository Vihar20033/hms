package com.hms.patient.repository;

import com.hms.patient.entity.Patient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PatientRepositoryImpl implements PatientRepositoryCustom {

    private final EntityManager entityManager;

    /**
     * Executes a dynamic multi-criteria search for Patients and returns a Sliced result set
     * to avoid unnecessary COUNT queries (Performance Optimization).
     */
    @Override
    public Slice<Patient> findAllAsSlice(Specification<Patient> spec, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Patient> cq = cb.createQuery(Patient.class);
        Root<Patient> root = cq.from(Patient.class);

        // Build core predicates from the provided Specification
        Predicate predicate = spec.toPredicate(root, cq, cb);
        if (predicate != null) {
            cq.where(predicate);
        }

        // Delegate search sort mapping to Spring Data QueryUtils to avoid manual criteria ordering logic
        cq.orderBy(QueryUtils.toOrders(pageable.getSort(), root, cb));

        TypedQuery<Patient> query = entityManager.createQuery(cq);
        
        // Execute the query using slice logic (pageSize + 1) to determine if there's more data
        int pageSize = pageable.getPageSize();
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageSize + 1);

        List<Patient> results = query.getResultList();
        boolean hasNext = results.size() > pageSize;

        // Strip the extra entry used for hasNext check and return standard Slice
        List<Patient> content = hasNext ? results.subList(0, pageSize) : results;

        return new SliceImpl<>(content, pageable, hasNext);
    }
}
