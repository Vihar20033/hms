package com.hms.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {
    private long totalPatients;
    private long todayAppointments;
    private long lowStockMedicines;
    private BigDecimal totalRevenue;
    private long patientsInQueue;
    private long completedConsultations;
    private long totalCompletedConsultations;
    private List<WeeklyStatisticsDTO> weeklyStats;
    private List<DepartmentStatisticsDTO> departmentStats;
}
