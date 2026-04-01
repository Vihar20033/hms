package com.hms.doctor.service.impl;

import com.hms.common.audit.AuditLogService;
import com.hms.common.enums.Department;
import com.hms.common.enums.Role;
import com.hms.common.util.SecurityUtils;
import com.hms.doctor.dto.request.CreateDoctorRequest;
import com.hms.doctor.dto.request.UpdateDoctorRequest;
import com.hms.doctor.dto.response.DoctorOnboardingResponse;
import com.hms.doctor.dto.response.DoctorResponseDTO;
import com.hms.doctor.entity.Doctor;
import com.hms.doctor.exception.DoctorNotFoundException;
import com.hms.doctor.mapper.DoctorMapper;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.doctor.service.DoctorService;
import com.hms.user.entity.User;
import com.hms.user.exception.EmailAlreadyExistsException;
import com.hms.user.exception.UsernameAlreadyExistsException;
import com.hms.user.repository.UserRepository;
import com.hms.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final DoctorMapper doctorMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public DoctorOnboardingResponse createDoctor(CreateDoctorRequest request) {
        
        Long userId = request.getUserId();
        if (userId == null) {
            userRepository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
                throw new EmailAlreadyExistsException("Email already exists");
            });

            userRepository.findByUsername(request.getUsername()).ifPresent(existingUser -> {
                throw new UsernameAlreadyExistsException("Username already exists");
            });

            User user = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getTemporaryPassword()))
                    .role(Role.DOCTOR)
                    .enabled(true)
                    .passwordChangeRequired(true)
                    .build();
            
            User savedUser = userRepository.save(user);
            userId = savedUser.getId();
        }

        Doctor doctor = doctorMapper.toEntity(request);
        doctor.setUserId(userId);
        
        if (doctor.getRegistrationNumber() == null) {
            doctor.setRegistrationNumber(request.getLicenseNumber());
        }
        
        Doctor savedDoctor = doctorRepository.save(doctor);
        auditLogService.log(SecurityUtils.getCurrentUsername(), "DOCTOR_CREATE", "Doctor", savedDoctor.getId().toString(), "name=" + savedDoctor.getFirstName() + " " + savedDoctor.getLastName());
        DoctorResponseDTO doctorDto = doctorMapper.toDto(savedDoctor);

        return DoctorOnboardingResponse.builder()
                .doctor(doctorDto)
                .username(request.getUsername())
                .temporaryPassword(request.getTemporaryPassword())
                .passwordChangeRequired(true)
                .build();
    }

    @Override
    @Transactional
    public Doctor updateDoctor(Long id, UpdateDoctorRequest request) {
        Doctor doctor = getDoctorById(id);
        doctorMapper.updateEntity(request, doctor);
        Doctor saved = doctorRepository.save(doctor);
        auditLogService.log(SecurityUtils.getCurrentUsername(), "DOCTOR_UPDATE", "Doctor", id.toString(), "name=" + saved.getFirstName() + " " + saved.getLastName());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> getDoctorsByDepartment(Department department) {
        return doctorRepository.findByDepartment(department);
    }

    @Override
    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = getDoctorById(id);
        doctorRepository.delete(doctor);
        auditLogService.log(SecurityUtils.getCurrentUsername(), "DOCTOR_DELETE", "Doctor", id.toString(), "name=" + doctor.getFirstName() + " " + doctor.getLastName());

        userRepository.findById(doctor.getUserId()).ifPresent(user -> userService.deleteUser(user.getId()));
    }
}
