package com.hms.common.search.controller;

import com.hms.common.api.response.ApiResponse;
import com.hms.common.search.service.ElasticsearchReindexService;
import com.hms.common.search.service.ReindexStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Elasticsearch admin operations.
 * Provides endpoints for reindexing and managing search indices.
 * 
 * All endpoints require ADMIN role.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/search")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSearchController {

    private final ElasticsearchReindexService reindexService;

    /**
     * Reindex all patients.
     * Clears existing patient index and reindexes from database.
     * 
     * @return Number of patients reindexed
     */
    @PostMapping("/reindex/patients")
    public ResponseEntity<ApiResponse<Long>> reindexPatients() {
        log.info("Admin triggered patient reindex");
        try {
            long count = reindexService.reindexPatients();
            return ResponseEntity.ok(new ApiResponse<>(true, "Patients reindexed successfully", count));
        } catch (Exception e) {
            log.error("Error reindexing patients", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to reindex patients: " + e.getMessage(), null));
        }
    }

    /**
     * Reindex all doctors.
     * Clears existing doctor index and reindexes from database.
     * 
     * @return Number of doctors reindexed
     */
    @PostMapping("/reindex/doctors")
    public ResponseEntity<ApiResponse<Long>> reindexDoctors() {
        log.info("Admin triggered doctor reindex");
        try {
            long count = reindexService.reindexDoctors();
            return ResponseEntity.ok(new ApiResponse<>(true, "Doctors reindexed successfully", count));
        } catch (Exception e) {
            log.error("Error reindexing doctors", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to reindex doctors: " + e.getMessage(), null));
        }
    }

    /**
     * Reindex all appointments.
     * Clears existing appointment index and reindexes from database.
     * 
     * @return Number of appointments reindexed
     */
    @PostMapping("/reindex/appointments")
    public ResponseEntity<ApiResponse<Long>> reindexAppointments() {
        log.info("Admin triggered appointment reindex");
        try {
            long count = reindexService.reindexAppointments();
            return ResponseEntity.ok(new ApiResponse<>(true, "Appointments reindexed successfully", count));
        } catch (Exception e) {
            log.error("Error reindexing appointments", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to reindex appointments: " + e.getMessage(), null));
        }
    }

    /**
     * Reindex all prescriptions.
     * Clears existing prescription index and reindexes from database.
     * 
     * @return Number of prescriptions reindexed
     */
    @PostMapping("/reindex/prescriptions")
    public ResponseEntity<ApiResponse<Long>> reindexPrescriptions() {
        log.info("Admin triggered prescription reindex");
        try {
            long count = reindexService.reindexPrescriptions();
            return ResponseEntity.ok(new ApiResponse<>(true, "Prescriptions reindexed successfully", count));
        } catch (Exception e) {
            log.error("Error reindexing prescriptions", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to reindex prescriptions: " + e.getMessage(), null));
        }
    }

    /**
     * Perform a full reindex of all entities.
     * Reindexes patients, doctors, appointments, and prescriptions.
     * 
     * @return ReindexStatus with summary of all reindexed entities
     */
    @PostMapping("/reindex/all")
    public ResponseEntity<ApiResponse<ReindexStatus>> reindexAll() {
        log.info("Admin triggered full reindex");
        try {
            ReindexStatus status = reindexService.fullReindex();
            
            if ("SUCCESS".equals(status.getStatus())) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Full reindex completed successfully", status));
            } else {
                return ResponseEntity.internalServerError()
                        .body(new ApiResponse<>(false, "Full reindex failed: " + status.getErrorMessage(), status));
            }
        } catch (Exception e) {
            log.error("Error during full reindex", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to perform full reindex: " + e.getMessage(), null));
        }
    }

    /**
     * Clear all search indices.
     * USE WITH CAUTION - removes all indexed data.
     * 
     * @return Success message
     */
    @DeleteMapping("/indices/clear-all")
    public ResponseEntity<ApiResponse<String>> clearAllIndices() {
        log.warn("Admin triggered clearing of all indices");
        try {
            reindexService.clearAllIndices();
            return ResponseEntity.ok(new ApiResponse<>(true, "All indices cleared successfully", "Indices cleared"));
        } catch (Exception e) {
            log.error("Error clearing indices", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to clear indices: " + e.getMessage(), null));
        }
    }

    /**
     * Get Elasticsearch availability status.
     * Checks if Elasticsearch is reachable and operational.
     * 
     * @return true if available, false otherwise
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Boolean>> checkHealth() {
        try {
            boolean available = reindexService.isElasticsearchAvailable();
            if (available) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Elasticsearch is available", true));
            } else {
                return ResponseEntity.ok(new ApiResponse<>(false, "Elasticsearch is not available", false));
            }
        } catch (Exception e) {
            log.error("Error checking Elasticsearch health", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to check Elasticsearch health: " + e.getMessage(), false));
        }
    }
}
