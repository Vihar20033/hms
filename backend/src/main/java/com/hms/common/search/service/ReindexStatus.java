package com.hms.common.search.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for reindex operation status and results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReindexStatus {
    private long patientCount;
    private long doctorCount;
    private long appointmentCount;
    private long prescriptionCount;
    private long totalCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // SUCCESS, FAILED, PARTIAL, IN_PROGRESS
    private String errorMessage;
}
