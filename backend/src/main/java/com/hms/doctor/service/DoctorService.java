package com.hms.doctor.service;

import com.hms.doctor.dto.request.CreateDoctorRequest;
import com.hms.doctor.dto.request.UpdateDoctorRequest;
import com.hms.doctor.dto.response.DoctorOnboardingResponse;
import com.hms.doctor.entity.Doctor;
import java.util.List;
import java.util.UUID;

public interface DoctorService {
    DoctorOnboardingResponse createDoctor(CreateDoctorRequest request);

    Doctor updateDoctor(UUID id, UpdateDoctorRequest request);

    Doctor getDoctorById(UUID id);

    Doctor getDoctorByUserId(UUID userId);

    List<Doctor> getAllDoctors();

    List<Doctor> getDoctorsByDepartment(com.hms.common.enums.Department department);

    void deleteDoctor(UUID id);
}
