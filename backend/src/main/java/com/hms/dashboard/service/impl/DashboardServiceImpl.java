package com.hms.dashboard.service.impl;

import com.hms.appointment.repository.AppointmentRepository;
import com.hms.billing.repository.BillingRepository;
import com.hms.common.enums.AppointmentStatus;
import com.hms.dashboard.dto.DashboardSummaryDTO;
import com.hms.dashboard.dto.WeeklyStatisticsDTO;
import com.hms.dashboard.service.DashboardService;
import com.hms.doctor.repository.DoctorRepository;
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

        @Override
        public DashboardSummaryDTO getSummary() {
                LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

                long totalPatients = patientRepository.count();
                long todayAppointments = appointmentRepository.countByAppointmentTimeBetween(startOfDay, endOfDay);
                long totalDoctors = doctorRepository.count();

                long lowStock = medicineRepository.countLowStock();

                BigDecimal totalRevenue = billingRepository.sumTotalRevenue();
                if (totalRevenue == null)
                        totalRevenue = BigDecimal.ZERO;

                long inQueue = appointmentRepository.countByStatusInAndAppointmentTimeBetween(
                                Arrays.asList(AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_CONSULTATION,
                                                AppointmentStatus.CONFIRMED),
                                startOfDay, endOfDay);

                long consultations = appointmentRepository.countByStatusAndUpdatedAtBetween(
                                AppointmentStatus.COMPLETED,
                                startOfDay, endOfDay);

                long totalConsultations = appointmentRepository.countByStatus(AppointmentStatus.COMPLETED);

                List<WeeklyStatisticsDTO> weeklyStats = new ArrayList<>();
                // Generate a 7-day window centered on today (-3 to +3 days)
                // This shows recent trends and upcoming bookings (total 7 days)
                for (int i = -3; i <= 3; i++) {
                        LocalDate date = LocalDate.now().plusDays(i);
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

                                .totalRevenue(totalRevenue)
                                .patientsInQueue(inQueue)
                                .completedConsultations(consultations)
                                .totalCompletedConsultations(totalConsultations)
                                .weeklyStats(weeklyStats)
                                .build();
        }
}
