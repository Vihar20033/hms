package com.hms.common.specification;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class SearchSpecification<T> {

    public static <T> Specification<T> fuzzySearch(String query, List<String> searchFields) {
        return (root, criteriaQuery, cb) -> {
            if (query == null || query.isEmpty() || searchFields == null || searchFields.isEmpty()) {
                return cb.conjunction();
            }

            String pattern = "%" + query.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            for (String field : searchFields) {
                predicates.add(cb.like(cb.lower(root.get(field)), pattern));
            }

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static <T> Specification<T> multiColumnFilter(java.util.Map<String, String> filters) {
        return (root, criteriaQuery, cb) -> {
            if (filters == null || filters.isEmpty()) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();
            filters.forEach((field, value) -> {
                if (value != null && !value.isEmpty()) {
                    predicates.add(cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%"));
                }
            });

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
