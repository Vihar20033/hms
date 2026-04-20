package com.hms.doctor.service.impl;

import com.hms.audit.service.AuditLogService;
import com.hms.common.enums.Department;
import com.hms.common.enums.Role;
import com.hms.security.util.SecurityUtils;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import com.hms.common.enums.AppointmentStatus;
import com.hms.common.exception.ConflictException;
import com.hms.appointment.repository.AppointmentRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final AppointmentRepository appointmentRepository;

    @Override
    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
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
    @CacheEvict(value = "doctors", allEntries = true)
    public Doctor updateDoctor(Long id, UpdateDoctorRequest request) {
        Doctor doctor = getDoctorById(id);
        doctorMapper.updateEntity(request, doctor);
        Doctor saved = doctorRepository.save(doctor);
        auditLogService.log(SecurityUtils.getCurrentUsername(), "DOCTOR_UPDATE", "Doctor", id.toString(), "name=" + saved.getFirstName() + " " + saved.getLastName());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "doctors", key = "#id")
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "doctors", key = "'all'")
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "doctors", key = "'slice_' + #page + '_' + #size")
    public Slice<Doctor> getDoctorSlice(int page, int size) {
        return getDoctorSlice(page, size, null);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "doctors", key = "'slice_' + #page + '_' + #size + '_' + (#query == null ? '' : #query)")
    public Slice<Doctor> getDoctorSlice(int page, int size, String query) {
        PageRequest request = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "lastName", "firstName"));
        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.isEmpty()) {
            return doctorRepository.findAll(request);
        }
        return doctorRepository.searchDoctors(normalizedQuery, request);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "doctors", key = "'dept_' + #department")
    public List<Doctor> getDoctorsByDepartment(Department department) {
        return doctorRepository.findByDepartment(department);
    }

    @Override
    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public void deleteDoctor(Long id) {
        Doctor doctor = getDoctorById(id);

        long activeAppointments = getAppointmentCount(id);

        if (activeAppointments > 0) {
            throw new ConflictException("Cannot delete doctor with active or upcoming appointments. Please reassign or cancel them first.");
        }

        doctorRepository.delete(doctor);
        auditLogService.log(SecurityUtils.getCurrentUsername(), "DOCTOR_DELETE", "Doctor", id.toString(), "name=" + doctor.getFirstName() + " " + doctor.getLastName());
        userRepository.findById(doctor.getUserId()).ifPresent(user -> userService.deleteUser(user.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public long getAppointmentCount(Long id) {
        return appointmentRepository.countByDoctorIdAndStatusIn(id,
                java.util.List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED, AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_CONSULTATION));
    }
}
