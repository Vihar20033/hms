package com.hms.reporting.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class HospitalPerformanceDTO {
    private long totalPatients;
    private long totalDoctors;
    private long totalAppointments;
    private BigDecimal totalRevenue;
    private List<DepartmentStat> departmentStats;
    private List<DailyRevenue> dailyRevenue;

    @Data
    @Builder
    public static class DepartmentStat {
        private String departmentName;
        private long doctorCount;
        private long patientCount;
    }

    @Data
    @Builder
    public static class DailyRevenue {
        private LocalDateTime date;
        private BigDecimal revenue;
    }
}
