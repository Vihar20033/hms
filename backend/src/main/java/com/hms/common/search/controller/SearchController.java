package com.hms.common.search.controller;

import com.hms.common.api.response.ApiResponse;
import com.hms.common.search.document.AppointmentDocument;
import com.hms.common.search.document.DoctorDocument;
import com.hms.common.search.document.PatientDocument;
import com.hms.common.search.document.PrescriptionDocument;
import com.hms.common.search.dto.SearchResultDTO;
import com.hms.common.search.service.impl.AppointmentSearchServiceImpl;
import com.hms.common.search.service.impl.DoctorSearchServiceImpl;
import com.hms.common.search.service.impl.PatientSearchServiceImpl;
import com.hms.common.search.service.impl.PrescriptionSearchServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for global search operations.
 * Provides endpoints for searching patients, doctors, appointments, and prescriptions.
 * Uses fuzzy search with typo tolerance and phonetic matching.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final PatientSearchServiceImpl patientSearchService;
    private final DoctorSearchServiceImpl doctorSearchService;
    private final AppointmentSearchServiceImpl appointmentSearchService;
    private final PrescriptionSearchServiceImpl prescriptionSearchService;

    /**
     * Search for patients using fuzzy search.
     * Supports typo tolerance - searching "John" will also find "Jon", "Johan", etc.
     * 
     * @param query Search query (supports name, contact number, email)
     * @param page Page number (0-based)
     * @param size Page size
     * @return List of matching patients
     */
    @GetMapping("/patients")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'PHARMACIST', 'LABORATORY_STAFF')")
    public ResponseEntity<ApiResponse<SearchResultDTO<PatientDocument>>> searchPatients(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("Searching patients with query: {}", query);
        try {
            SearchResultDTO<PatientDocument> results = patientSearchService.fuzzySearch(query, page, size);
            return ResponseEntity.ok(new ApiResponse<>(true, "Patients found", results));
        } catch (Exception e) {
            log.error("Error searching patients", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Search failed: " + e.getMessage(), null));
        }
    }

    /**
     * Search for doctors using fuzzy search.
     * Supports searching by name, specialization, qualification, department, etc.
     * 
     * @param query Search query
     * @param page Page number (0-based)
     * @param size Page size
     * @return List of matching doctors
     */
    @GetMapping("/doctors")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<SearchResultDTO<DoctorDocument>>> searchDoctors(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("Searching doctors with query: {}", query);
        try {
            SearchResultDTO<DoctorDocument> results = doctorSearchService.fuzzySearch(query, page, size);
            return ResponseEntity.ok(new ApiResponse<>(true, "Doctors found", results));
        } catch (Exception e) {
            log.error("Error searching doctors", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Search failed: " + e.getMessage(), null));
        }
    }

    /**
     * Search for appointments using fuzzy search.
     * Supports searching by patient name, doctor name, reason, department, etc.
     * 
     * @param query Search query
     * @param page Page number (0-based)
     * @param size Page size
     * @return List of matching appointments
     */
    @GetMapping("/appointments")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<SearchResultDTO<AppointmentDocument>>> searchAppointments(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("Searching appointments with query: {}", query);
        try {
            SearchResultDTO<AppointmentDocument> results = appointmentSearchService.fuzzySearch(query, page, size);
            return ResponseEntity.ok(new ApiResponse<>(true, "Appointments found", results));
        } catch (Exception e) {
            log.error("Error searching appointments", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Search failed: " + e.getMessage(), null));
        }
    }

    /**
     * Search for prescriptions using fuzzy search.
     * Supports searching by patient name, doctor name, diagnosis, medicines, symptoms, etc.
     * 
     * @param query Search query
     * @param page Page number (0-based)
     * @param size Page size
     * @return List of matching prescriptions
     */
    @GetMapping("/prescriptions")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<SearchResultDTO<PrescriptionDocument>>> searchPrescriptions(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("Searching prescriptions with query: {}", query);
        try {
            SearchResultDTO<PrescriptionDocument> results = prescriptionSearchService.fuzzySearch(query, page, size);
            return ResponseEntity.ok(new ApiResponse<>(true, "Prescriptions found", results));
        } catch (Exception e) {
            log.error("Error searching prescriptions", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Search failed: " + e.getMessage(), null));
        }
    }

    /**
     * Perform exact field search for patients.
     * Use this for precise matching.
     * 
     * @param field Field name (e.g., "contactNumber", "email")
     * @param value Exact value to match
     * @param page Page number (0-based)
     * @param size Page size
     * @return List of matching patients
     */
    @GetMapping("/patients/exact")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<SearchResultDTO<PatientDocument>>> searchPatientsExact(
            @RequestParam String field,
            @RequestParam String value,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("Searching patients with exact field: {}, value: {}", field, value);
        try {
            SearchResultDTO<PatientDocument> results = patientSearchService.exactSearch(field, value, page, size);
            return ResponseEntity.ok(new ApiResponse<>(true, "Patients found", results));
        } catch (Exception e) {
            log.error("Error searching patients by exact field", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Search failed: " + e.getMessage(), null));
        }
    }

    /**
     * Perform exact field search for doctors.
     * Use this for precise matching.
     * 
     * @param field Field name (e.g., "department", "specialization")
     * @param value Exact value to match
     * @param page Page number (0-based)
     * @param size Page size
     * @return List of matching doctors
     */
    @GetMapping("/doctors/exact")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<SearchResultDTO<DoctorDocument>>> searchDoctorsExact(
            @RequestParam String field,
            @RequestParam String value,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("Searching doctors with exact field: {}, value: {}", field, value);
        try {
            SearchResultDTO<DoctorDocument> results = doctorSearchService.exactSearch(field, value, page, size);
            return ResponseEntity.ok(new ApiResponse<>(true, "Doctors found", results));
        } catch (Exception e) {
            log.error("Error searching doctors by exact field", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Search failed: " + e.getMessage(), null));
        }
    }
}
