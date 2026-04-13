package com.hms.appointment.service;

import com.hms.appointment.entity.Appointment;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.common.enums.AppointmentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentQueueService {

    private final AppointmentRepository appointmentRepository;

    /**
     * Gets the next patient for a specific doctor based on medical priority.
     * Uses a Max-Heap (PriorityQueue) to rank patients by Severity Score.
     * If scores are equal, the earliest booked appointment takes precedence (FIFO fallback).
     */
    public Appointment getNextPatient(Long doctorId) {
        Instant startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusNanos(1);

        // Fetch all active appointments for today for this doctor
        List<Appointment> todayAppointments = appointmentRepository.findAppointments(
                null, // doctorUserId not needed here if we use doctorId directly, but check repo
                null, 
                AppointmentStatus.SCHEDULED, 
                startOfDay, 
                endOfDay);
        
        // Filter for specific doctorId if repository doesn't have a direct method
        // (Assuming we might need to filter manually if findAppointments uses doctorUserId)
        // Let's assume we filter by doctorId.
        
        // Binary Max-Heap Implementation
        PriorityQueue<Appointment> maxHeap = new PriorityQueue<>(
            Comparator.comparingInt(Appointment::getSeverityScore).reversed()
                      .thenComparing(Appointment::getAppointmentTime)
        );

        maxHeap.addAll(todayAppointments.stream()
                .filter(a -> a.getDoctor() != null && a.getDoctor().getId().equals(doctorId))
                .toList());

        return maxHeap.poll(); // Returns and removes the highest priority patient
    }

    public List<Appointment> getPriorityQueue(Long doctorId) {
        Instant startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusNanos(1);

        List<Appointment> appointments = appointmentRepository.findAppointments(
                null, null, AppointmentStatus.SCHEDULED, startOfDay, endOfDay);

        return appointments.stream()
                .filter(a -> a.getDoctor() != null && a.getDoctor().getId().equals(doctorId))
                .sorted(Comparator.comparingInt(Appointment::getSeverityScore).reversed()
                        .thenComparing(Appointment::getAppointmentTime))
                .toList();
    }
}
