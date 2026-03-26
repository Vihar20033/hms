package com.hms.patient.specification;

import com.hms.common.enums.BloodGroup;
import com.hms.patient.entity.Patient;
import com.hms.common.enums.UrgencyLevel;
import org.springframework.data.jpa.domain.Specification;

public class PatientSpecification {

    // WHERE LOWER(name) LIKE '%john%'
    public static Specification<Patient> hasName(String name) {
        return (root, query, cb) ->
                (name == null || name.isBlank()) ? null :
                        cb.like(cb.lower(root.get("name")),
                                "%" + name.toLowerCase() + "%");
    }

    // WHERE blood_group = 'A_POSITIVE'
    public static Specification<Patient> hasBloodGroup(String bloodGroup) {
        return (root, query, cb) -> {
            if (bloodGroup == null || bloodGroup.isBlank()) {
                return null;
            }
            try {
                // Try matching by enum name or enum label
                for (BloodGroup bg : BloodGroup.values()) {
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

    // WHERE email = 'abc@gmail.com'
    public static Specification<Patient> hasEmail(String email) {
        return (root, query, cb) ->
                (email == null || email.isBlank()) ? null :
                        cb.equal(root.get("email"), email);
    }

    /**
     * Advanced Fuzzy Search:
     * Searches across name, email, and contactNumber using LIKE and SOUNDEX.
     */
    public static Specification<Patient> fuzzySearch(String query) {
        return (root, query1, cb) -> {
            if (query == null || query.isBlank()) {
                return null;
            }
            
            String pattern = "%" + query.toLowerCase() + "%";
            
            // Name LIKE %query% OR email LIKE %query% OR contactNumber LIKE %query%
            Specification<Patient> likeName = (root1, query2, cb1) -> cb1.like(cb1.lower(root1.get("name")), pattern);
            Specification<Patient> likeEmail = (root1, query2, cb1) -> cb1.like(cb1.lower(root1.get("email")), pattern);
            Specification<Patient> likeContact = (root1, query2, cb1) -> cb1.like(root1.get("contactNumber"), pattern);
            
            // For true fuzzy (phonetic), we can use SOUNDEX if the DB supports it
            Specification<Patient> soundexName = (root1, query2, cb1) -> cb1.equal(cb1.function("SOUNDEX", String.class, root1.get("name")), 
                                     cb1.function("SOUNDEX", String.class, cb1.literal(query)));

            return cb.or(
                likeName.toPredicate(root, query1, cb),
                likeEmail.toPredicate(root, query1, cb),
                likeContact.toPredicate(root, query1, cb),
                soundexName.toPredicate(root, query1, cb)
            );
        };
    }
}
