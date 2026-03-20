package com.hms.doctor.service.impl;

import com.hms.doctor.dto.request.DoctorScheduleRequestDTO;
import com.hms.doctor.dto.response.DoctorScheduleResponseDTO;
import com.hms.doctor.entity.Doctor;
import com.hms.doctor.entity.DoctorSchedule;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.doctor.repository.DoctorScheduleRepository;
import com.hms.doctor.service.DoctorScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private final DoctorScheduleRepository doctorScheduleRepository;
    private final DoctorRepository doctorRepository;

    @Override
    public DoctorScheduleResponseDTO createSchedule(DoctorScheduleRequestDTO request) {
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .filter(existingDoctor -> !existingDoctor.isDeleted())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        DoctorSchedule schedule = DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .slotDurationMinutes(request.getSlotDurationMinutes())
                .build();
                
        DoctorSchedule saved = doctorScheduleRepository.save(schedule);
        return mapToDto(saved);
    }

    @Override
    public List<DoctorScheduleResponseDTO> getDoctorSchedules(UUID doctorId) {
        return doctorScheduleRepository.findByDoctorId(doctorId).stream()
                .filter(schedule -> !schedule.isDeleted())
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorScheduleResponseDTO> getDoctorSchedulesByDay(UUID doctorId, DayOfWeek dayOfWeek) {
        return doctorScheduleRepository.findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek).stream()
                .filter(schedule -> !schedule.isDeleted())
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSchedule(UUID id) {
        DoctorSchedule schedule = doctorScheduleRepository.findById(id)
                .filter(existingSchedule -> !existingSchedule.isDeleted())
                .orElseThrow(() -> new RuntimeException("Doctor schedule not found"));
        schedule.setDeleted(true);
        doctorScheduleRepository.save(schedule);
    }
    
    private DoctorScheduleResponseDTO mapToDto(DoctorSchedule schedule) {
        DoctorScheduleResponseDTO dto = new DoctorScheduleResponseDTO();
        dto.setId(schedule.getId());
        dto.setDoctorId(schedule.getDoctor().getId());
        dto.setDoctorName(schedule.getDoctor().getFirstName() + " " + schedule.getDoctor().getLastName());
        dto.setDayOfWeek(schedule.getDayOfWeek());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setSlotDurationMinutes(schedule.getSlotDurationMinutes());
        return dto;
    }
}
