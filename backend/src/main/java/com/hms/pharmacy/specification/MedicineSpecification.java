package com.hms.pharmacy.specification;

import com.hms.pharmacy.entity.Medicine;
import com.hms.common.enums.MedicineCategory;
import org.springframework.data.jpa.domain.Specification;

public class MedicineSpecification {

    public static Specification<Medicine> hasCategory(MedicineCategory category) {
        return (root, query, cb) -> category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Medicine> isActive(Boolean active) {
        return (root, query, cb) -> active == null ? null : cb.equal(root.get("isActive"), active);
    }

    public static Specification<Medicine> fuzzySearch(String query) {
        return (root, query1, cb) -> {
            if (query == null || query.isBlank()) {
                return null;
            }

            String pattern = "%" + query.toLowerCase() + "%";

            // Basic LIKE search across multiple fields
            Specification<Medicine> likeName = (root1, query2, cb1) -> cb1.like(cb1.lower(root1.get("name")), pattern);
            Specification<Medicine> likeCode = (root1, query2, cb1) -> cb1.like(cb1.lower(root1.get("medicineCode")), pattern);
            Specification<Medicine> likeManufacturer = (root1, query2, cb1) -> cb1.like(cb1.lower(root1.get("manufacturer")), pattern);
            
            // Phonetic search for names
            Specification<Medicine> soundexName = (root1, query2, cb1) -> cb1.equal(
                cb1.function("SOUNDEX", String.class, root1.get("name")),
                cb1.function("SOUNDEX", String.class, cb1.literal(query))
            );

            return cb.or(
                likeName.toPredicate(root, query1, cb),
                likeCode.toPredicate(root, query1, cb),
                likeManufacturer.toPredicate(root, query1, cb),
                soundexName.toPredicate(root, query1, cb)
            );
        };
    }
}
