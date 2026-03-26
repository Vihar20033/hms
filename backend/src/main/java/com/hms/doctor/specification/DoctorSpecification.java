package com.hms.doctor.specification;

import com.hms.doctor.entity.Doctor;
import com.hms.common.enums.Department;
import org.springframework.data.jpa.domain.Specification;

public class DoctorSpecification {



    public static Specification<Doctor> hasDepartment(Department department) {
        return (root, query, cb) -> department == null ? null : cb.equal(root.get("department"), department);
    }

    public static Specification<Doctor> isAvailable(Boolean available) {
        return (root, query, cb) -> available == null ? null : cb.equal(root.get("isAvailable"), available);
    }

    public static Specification<Doctor> fuzzySearch(String query) {
        return (root, query1, cb) -> {
            if (query == null || query.isBlank()) {
                return null;
            }

            String pattern = "%" + query.toLowerCase() + "%";

            // Basic LIKE search
            Specification<Doctor> likeFirstName = (root1, query2, cb1) -> cb1.like(cb1.lower(root1.get("firstName")), pattern);
            Specification<Doctor> likeLastName = (root1, query2, cb1) -> cb1.like(cb1.lower(root1.get("lastName")), pattern);
            Specification<Doctor> likeSpecialization = (root1, query2, cb1) -> cb1.like(cb1.lower(root1.get("specialization")), pattern);
            Specification<Doctor> likeRegNumber = (root1, query2, cb1) -> cb1.like(cb1.lower(root1.get("registrationNumber")), pattern);

            // Phonetic search for names
            Specification<Doctor> soundexFirstName = (root1, query2, cb1) -> cb1.equal(
                cb1.function("SOUNDEX", String.class, root1.get("firstName")),
                cb1.function("SOUNDEX", String.class, cb1.literal(query))
            );
            Specification<Doctor> soundexLastName = (root1, query2, cb1) -> cb1.equal(
                cb1.function("SOUNDEX", String.class, root1.get("lastName")),
                cb1.function("SOUNDEX", String.class, cb1.literal(query))
            );

            return cb.or(
                likeFirstName.toPredicate(root, query1, cb),
                likeLastName.toPredicate(root, query1, cb),
                likeSpecialization.toPredicate(root, query1, cb),
                likeRegNumber.toPredicate(root, query1, cb),
                soundexFirstName.toPredicate(root, query1, cb),
                soundexLastName.toPredicate(root, query1, cb)
            );
        };
    }
}
