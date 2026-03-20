package com.hms.doctor.service;

import com.hms.doctor.dto.request.DoctorScheduleRequestDTO;
import com.hms.doctor.dto.response.DoctorScheduleResponseDTO;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

public interface DoctorScheduleService {
    DoctorScheduleResponseDTO createSchedule(DoctorScheduleRequestDTO request);
    List<DoctorScheduleResponseDTO> getDoctorSchedules(UUID doctorId);
    List<DoctorScheduleResponseDTO> getDoctorSchedulesByDay(UUID doctorId, DayOfWeek dayOfWeek);
    void deleteSchedule(UUID id);
}
