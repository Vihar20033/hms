package com.hms.doctor.service;

import com.hms.doctor.dto.request.CreateDoctorRequest;
import com.hms.doctor.dto.request.UpdateDoctorRequest;
import com.hms.doctor.dto.response.DoctorOnboardingResponse;
import com.hms.doctor.entity.Doctor;
import org.springframework.data.domain.Slice;

import java.util.List;


public interface DoctorService {
    DoctorOnboardingResponse createDoctor(CreateDoctorRequest request);

    Doctor updateDoctor(Long id, UpdateDoctorRequest request);

    Doctor getDoctorById(Long id);

    List<Doctor> getAllDoctors();

    Slice<Doctor> getDoctorSlice(int page, int size);

    List<Doctor> getDoctorsByDepartment(com.hms.common.enums.Department department);

    void deleteDoctor(Long id);
    long getAppointmentCount(Long id);
}
