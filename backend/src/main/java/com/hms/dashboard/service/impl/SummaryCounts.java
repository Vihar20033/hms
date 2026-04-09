package com.hms.dashboard.service.impl;

import java.math.BigDecimal;

public record SummaryCounts(
        long totalPatients,
        long todayAppointments,
        long lowStockMedicines,
        BigDecimal totalRevenue,
        long patientsInQueue) {
}
