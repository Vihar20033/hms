package com.hms.doctor.service.impl;

import com.hms.common.enums.Role;
import com.hms.doctor.dto.request.CreateDoctorRequest;
import com.hms.doctor.dto.request.UpdateDoctorRequest;
import com.hms.doctor.dto.response.DoctorOnboardingResponse;
import com.hms.doctor.dto.response.DoctorResponseDTO;
import com.hms.doctor.entity.Doctor;
import com.hms.doctor.mapper.DoctorMapper;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.doctor.service.DoctorService;
import com.hms.user.entity.User;
import com.hms.user.exception.EmailAlreadyExistsException;
import com.hms.user.exception.UsernameAlreadyExistsException;
import com.hms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorMapper doctorMapper;
    private final com.hms.common.audit.AuditLogService auditLogService;

    @Override
    @Transactional
    public DoctorOnboardingResponse createDoctor(CreateDoctorRequest request) {
        
        UUID userId = request.getUserId();
        if (userId == null) {
            userRepository.findByEmailIncludingDeleted(request.getEmail()).ifPresent(existingUser -> {
                if (!existingUser.isDeleted()) {
                    throw new EmailAlreadyExistsException("Email already exists");
                }
                retireUserIdentity(existingUser);
                userRepository.save(existingUser);
            });

            userRepository.findByUsernameIncludingDeleted(request.getUsername()).ifPresent(existingUser -> {
                if (!existingUser.isDeleted()) {
                    throw new UsernameAlreadyExistsException("Username already exists");
                }
                retireUserIdentity(existingUser);
                userRepository.save(existingUser);
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
        auditLogService.log(getCurrentUsername(), "DOCTOR_CREATE", "Doctor", savedDoctor.getId().toString(), "name=" + savedDoctor.getFirstName() + " " + savedDoctor.getLastName());
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
    public Doctor updateDoctor(UUID id, UpdateDoctorRequest request) {
        Doctor doctor = getDoctorById(id);
        doctorMapper.updateEntity(request, doctor);
        Doctor saved = doctorRepository.save(doctor);
        auditLogService.log(getCurrentUsername(), "DOCTOR_UPDATE", "Doctor", id.toString(), "name=" + saved.getFirstName() + " " + saved.getLastName());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Doctor getDoctorById(UUID id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Doctor getDoctorByUserId(UUID userId) {
        return doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found for user: " + userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> getDoctorsByDepartment(com.hms.common.enums.Department department) {
        return doctorRepository.findByDepartment(department);
    }

    @Override
    @Transactional
    public void deleteDoctor(UUID id) {
        Doctor doctor = getDoctorById(id);
        doctor.setDeleted(true);
        doctorRepository.save(doctor);
        auditLogService.log(getCurrentUsername(), "DOCTOR_DELETE", "Doctor", id.toString(), "name=" + doctor.getFirstName() + " " + doctor.getLastName());

        userRepository.findById(doctor.getUserId()).ifPresent(user -> {
            retireUserIdentity(user);
            user.setDeleted(true);
            user.setEnabled(false);
            userRepository.save(user);
        });
    }

    private String getCurrentUsername() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }

    private void retireUserIdentity(User user) {
        String uniqueSuffix = "__deleted__" + user.getId();

        if (user.getUsername() != null && !user.getUsername().contains("__deleted__")) {
            String retiredUsername = truncate(user.getUsername() + uniqueSuffix, 50);
            user.setUsername(retiredUsername);
        }

        if (user.getEmail() != null && !user.getEmail().contains("__deleted__")) {
            user.setEmail(user.getId() + "__deleted__" + user.getEmail());
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
