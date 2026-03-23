package com.hms.patient.repository;

import com.hms.patient.entity.Patient;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;

public interface PatientRepositoryCustom {
    Slice<Patient> findAllAsSlice(Specification<Patient> spec, Pageable pageable);
}