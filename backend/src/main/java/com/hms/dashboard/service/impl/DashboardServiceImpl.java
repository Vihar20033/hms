package com.hms.dashboard.service.impl;

import com.hms.appointment.repository.AppointmentRepository;
import com.hms.billing.repository.BillingRepository;
import com.hms.common.enums.AppointmentStatus;
import com.hms.common.enums.Department;
import com.hms.dashboard.dto.DashboardSummaryDTO;
import com.hms.dashboard.dto.DepartmentStatisticsDTO;
import com.hms.dashboard.dto.WeeklyStatisticsDTO;
import com.hms.dashboard.service.DashboardService;
import com.hms.patient.repository.PatientRepository;
import com.hms.pharmacy.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

        private final PatientRepository patientRepository;
        private final AppointmentRepository appointmentRepository;
        private final MedicineRepository medicineRepository;
        private final BillingRepository billingRepository;

        @Autowired
        public DashboardServiceImpl(
                        PatientRepository patientRepository,
                        AppointmentRepository appointmentRepository,
                        MedicineRepository medicineRepository,
                        BillingRepository billingRepository) {
                this.patientRepository = patientRepository;
                this.appointmentRepository = appointmentRepository;
                this.medicineRepository = medicineRepository;
                this.billingRepository = billingRepository;
        }

        @Override
        public DashboardSummaryDTO getSummary() {
                Instant startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
                Instant endOfDay = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusNanos(1);

                SummaryCounts counts = loadSummaryCounts(startOfDay, endOfDay);

                // Consultations (can be added to procedure later if needed, currently kept for compatibility)
                long consultations = appointmentRepository.countByStatusAndUpdatedAtBetween(
                                AppointmentStatus.COMPLETED,
                                startOfDay, endOfDay);

                long totalConsultations = appointmentRepository.countByStatus(AppointmentStatus.COMPLETED);

                List<WeeklyStatisticsDTO> weeklyStats = new ArrayList<>();
                // Generate a 7-day window centered on today (-3 to +3 days)
                for (int i = -3; i <= 3; i++) {
                        LocalDate date = LocalDate.now().plusDays(i);
                        Instant start = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
                        Instant end = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusNanos(1);

                        weeklyStats.add(WeeklyStatisticsDTO.builder()
                                        .day(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                                        .appointments(appointmentRepository.countByAppointmentTimeBetween(start, end))
                                        .patients(patientRepository.countByCreatedAtBetween(start, end))
                                        .build());
                }

                // Generate department-wise stats
                List<DepartmentStatisticsDTO> deptStats = new ArrayList<>();
                for (Department dept : Department.values()) {
                        long count = appointmentRepository.countByDepartment(dept);
                        if (count > 0) {
                                deptStats.add(DepartmentStatisticsDTO.builder()
                                                .department(dept.name())
                                                .appointmentCount(count)
                                                .build());
                        }
                }

                return DashboardSummaryDTO.builder()
                                .totalPatients(counts.totalPatients())
                                .todayAppointments(counts.todayAppointments())
                                .lowStockMedicines(counts.lowStockMedicines())
                                .totalRevenue(counts.totalRevenue())
                                .patientsInQueue(counts.patientsInQueue())
                                .completedConsultations(consultations)
                                .totalCompletedConsultations(totalConsultations)
                                .weeklyStats(weeklyStats)
                                .departmentStats(deptStats)
                                .build();
        }

        private SummaryCounts loadSummaryCounts(Instant startOfDay, Instant endOfDay) {
                return new SummaryCounts(
                                patientRepository.count(),
                                appointmentRepository.countByAppointmentTimeBetween(startOfDay, endOfDay),
                                medicineRepository.countLowStock(),
                                defaultRevenue(billingRepository.sumTotalRevenue()),
                                appointmentRepository.countByStatus(AppointmentStatus.CHECKED_IN));
        }

        private BigDecimal defaultRevenue(BigDecimal value) {
                return value != null ? value : BigDecimal.ZERO;
        }

        private record SummaryCounts(
                        long totalPatients,
                        long todayAppointments,
                        long lowStockMedicines,
                        BigDecimal totalRevenue,
                        long patientsInQueue) {
        }
}

