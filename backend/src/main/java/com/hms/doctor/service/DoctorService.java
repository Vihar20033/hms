package com.hms.doctor.service;

import com.hms.doctor.dto.request.CreateDoctorRequest;
import com.hms.doctor.dto.request.UpdateDoctorRequest;
import com.hms.doctor.dto.response.DoctorOnboardingResponse;
import com.hms.doctor.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface DoctorService {
    DoctorOnboardingResponse createDoctor(CreateDoctorRequest request);

    Doctor updateDoctor(Long id, UpdateDoctorRequest request);

    Doctor getDoctorById(Long id);

    Doctor getDoctorByUserId(Long userId);

    List<Doctor> getAllDoctors();

    List<Doctor> getDoctorsByDepartment(com.hms.common.enums.Department department);

    Page<Doctor> searchDoctors(
            String query,
            com.hms.common.enums.Department department,
            Boolean isAvailable,
            Pageable pageable
    );

    void deleteDoctor(Long id);
}
