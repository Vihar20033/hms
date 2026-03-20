package com.hms.patient.specification;

import com.hms.patient.entity.Patient;
import com.hms.common.enums.UrgencyLevel;
import org.springframework.data.jpa.domain.Specification;

public class PatientSpecification {

    public static Specification<Patient> notDeleted() {
        return (root, query, cb) ->
                cb.isFalse(root.get("deleted"));
    }

    public static Specification<Patient> hasName(String name) {
        return (root, query, cb) ->
                (name == null || name.isBlank()) ? null :
                        cb.like(cb.lower(root.get("name")),
                                "%" + name.toLowerCase() + "%");
    }

    public static Specification<Patient> hasBloodGroup(String bloodGroup) {
        return (root, query, cb) -> {
            if (bloodGroup == null || bloodGroup.isBlank()) {
                return null;
            }
            try {
                // Try matching by enum name or enum label
                for (com.hms.common.enums.BloodGroup bg : com.hms.common.enums.BloodGroup.values()) {
                    if (bg.getLabel().equalsIgnoreCase(bloodGroup) || bg.name().equalsIgnoreCase(bloodGroup)) {
                        return cb.equal(root.get("bloodGroup"), bg);
                    }
                }
                return null;
            } catch (Exception e) {
                return null;
            }
        };
    }

    public static Specification<Patient> hasUrgencyLevel(String urgencyLevel) {
        return (root, query, cb) -> {
            if (urgencyLevel == null || urgencyLevel.isBlank()) {
                return null;
            }
            try {
                return cb.equal(
                        root.get("urgencyLevel"),
                        UrgencyLevel.valueOf(urgencyLevel.toUpperCase())
                );
            } catch (IllegalArgumentException e) {
                return null;
            }
        };
    }

    public static Specification<Patient> hasEmail(String email) {
        return (root, query, cb) ->
                (email == null || email.isBlank()) ? null :
                        cb.equal(root.get("email"), email);
    }
}
