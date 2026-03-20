package com.hms.dashboard.service.impl;

import com.hms.appointment.repository.AppointmentRepository;
import com.hms.billing.repository.BillingRepository;
import com.hms.common.enums.AppointmentStatus;
import com.hms.common.enums.TestStatus;
import com.hms.dashboard.dto.DashboardSummaryDTO;
import com.hms.dashboard.dto.WeeklyStatisticsDTO;
import com.hms.dashboard.service.DashboardService;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.laboratory.repository.LabTestRepository;
import com.hms.patient.repository.PatientRepository;
import com.hms.pharmacy.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final MedicineRepository medicineRepository;
    private final BillingRepository billingRepository;
    private final LabTestRepository labRepository;

    @Override
    public DashboardSummaryDTO getSummary() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        long totalPatients = patientRepository.count();
        long todayAppointments = appointmentRepository.countByAppointmentTimeBetween(startOfDay, endOfDay);
        long totalDoctors = doctorRepository.count();
        
        long lowStock = medicineRepository.findAll().stream()
                .filter(m -> m.getQuantityInStock() <= (m.getReorderLevel() != null ? m.getReorderLevel() : 10))
                .count();

        BigDecimal todayRevenue = billingRepository.findAll().stream()
                .filter(b -> b.getCreatedAt() != null && b.getCreatedAt().isAfter(startOfDay))
                .filter(b -> b.getPaymentStatus() == com.hms.common.enums.PaymentStatus.PAID)
                .map(b -> b.getNetAmount() != null ? b.getNetAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRevenue = billingRepository.findAll().stream()
                .filter(b -> b.getPaymentStatus() == com.hms.common.enums.PaymentStatus.PAID)
                .map(b -> b.getNetAmount() != null ? b.getNetAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingLabs = labRepository.findAll().stream()
                .filter(l -> l.getStatus() == TestStatus.PENDING || l.getStatus() == TestStatus.IN_PROGRESS)
                .count();

        // New Stats
        long inQueue = appointmentRepository.countByStatusInAndAppointmentTimeBetween(
                Arrays.asList(AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_CONSULTATION, AppointmentStatus.CONFIRMED),
                startOfDay, endOfDay
        );

        long consultations = appointmentRepository.countByStatusAndAppointmentTimeBetween(
                AppointmentStatus.COMPLETED,
                startOfDay, endOfDay
        );

        long totalConsultations = appointmentRepository.findAll().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .count();

        // Weekly Stats for Charts
        List<WeeklyStatisticsDTO> weeklyStats = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);
            
            weeklyStats.add(WeeklyStatisticsDTO.builder()
                    .day(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .appointments(appointmentRepository.countByAppointmentTimeBetween(start, end))
                    .patients(patientRepository.countByCreatedAtBetween(start, end))
                    .build());
        }

        return DashboardSummaryDTO.builder()
                .totalPatients(totalPatients)
                .todayAppointments(todayAppointments)
                .totalDoctors(totalDoctors)
                .lowStockMedicines(lowStock)
                .todayRevenue(todayRevenue)
                .totalRevenue(totalRevenue)
                .pendingLabTests(pendingLabs)
                .patientsInQueue(inQueue)
                .completedConsultations(consultations)
                .totalCompletedConsultations(totalConsultations)
                .weeklyStats(weeklyStats)
                .build();
    }
}
