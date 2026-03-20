package com.hms.patient.repository;

import com.hms.patient.entity.Patient;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;

/**
 * Custom repository for efficient pagination using Slice (no count query).
 */
public interface PatientRepositoryCustom {

    /**
     * Fetch patients using Slice pagination without COUNT query
     */
    Slice<Patient> findAllAsSlice(Specification<Patient> spec, Pageable pageable);

}