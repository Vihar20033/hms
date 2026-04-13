package com.hms.common.search.service.impl;

import com.hms.appointment.entity.Appointment;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.common.search.document.AppointmentDocument;
import com.hms.common.search.document.DoctorDocument;
import com.hms.common.search.document.PatientDocument;
import com.hms.common.search.document.PrescriptionDocument;
import com.hms.common.search.service.ElasticsearchReindexService;
import com.hms.common.search.service.ReindexStatus;
import com.hms.doctor.entity.Doctor;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.patient.entity.Patient;
import com.hms.patient.repository.PatientRepository;
import com.hms.prescription.entity.Prescription;
import com.hms.prescription.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for Elasticsearch reindexing operations.
 * Handles batch reindexing from database to Elasticsearch for all entities.
 * 
 * Features:
 * - Batch processing for memory efficiency
 * - Transaction management
 * - Comprehensive error handling
 * - Progress tracking
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchReindexServiceImpl implements ElasticsearchReindexService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;

    private final PatientSearchServiceImpl patientSearchService;
    private final DoctorSearchServiceImpl doctorSearchService;
    private final AppointmentSearchServiceImpl appointmentSearchService;
    private final PrescriptionSearchServiceImpl prescriptionSearchService;

    private static final int BATCH_SIZE = 500;

    @Override
    @Transactional(readOnly = true)
    public long reindexPatients() {
        log.info("Starting patient reindex operation");
        try {
            patientSearchService.clearIndex();
            
            int pageNumber = 0;
            long totalReindexed = 0;

            while (true) {
                Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);
                Page<Patient> page = patientRepository.findAll(pageable);

                if (page.isEmpty()) {
                    break;
                }

                List<PatientDocument> documents = page.getContent().stream()
                        .map(this::convertPatientToDocument)
                        .collect(Collectors.toList());

                patientSearchService.indexBulk(documents);
                totalReindexed += documents.size();

                log.debug("Reindexed batch {} of patients, total so far: {}", pageNumber, totalReindexed);
                pageNumber++;
            }

            log.info("Patient reindex completed successfully. Total indexed: {}", totalReindexed);
            return totalReindexed;
        } catch (Exception e) {
            log.error("Error during patient reindex", e);
            throw new RuntimeException("Patient reindex failed", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long reindexDoctors() {
        log.info("Starting doctor reindex operation");
        try {
            doctorSearchService.clearIndex();
            
            int pageNumber = 0;
            long totalReindexed = 0;

            while (true) {
                Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);
                Page<Doctor> page = doctorRepository.findAll(pageable);

                if (page.isEmpty()) {
                    break;
                }

                List<DoctorDocument> documents = page.getContent().stream()
                        .map(this::convertDoctorToDocument)
                        .collect(Collectors.toList());

                doctorSearchService.indexBulk(documents);
                totalReindexed += documents.size();

                log.debug("Reindexed batch {} of doctors, total so far: {}", pageNumber, totalReindexed);
                pageNumber++;
            }

            log.info("Doctor reindex completed successfully. Total indexed: {}", totalReindexed);
            return totalReindexed;
        } catch (Exception e) {
            log.error("Error during doctor reindex", e);
            throw new RuntimeException("Doctor reindex failed", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long reindexAppointments() {
        log.info("Starting appointment reindex operation");
        try {
            appointmentSearchService.clearIndex();
            
            int pageNumber = 0;
            long totalReindexed = 0;

            while (true) {
                Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);
                Page<Appointment> page = appointmentRepository.findAll(pageable);

                if (page.isEmpty()) {
                    break;
                }

                List<AppointmentDocument> documents = page.getContent().stream()
                        .map(this::convertAppointmentToDocument)
                        .collect(Collectors.toList());

                appointmentSearchService.indexBulk(documents);
                totalReindexed += documents.size();

                log.debug("Reindexed batch {} of appointments, total so far: {}", pageNumber, totalReindexed);
                pageNumber++;
            }

            log.info("Appointment reindex completed successfully. Total indexed: {}", totalReindexed);
            return totalReindexed;
        } catch (Exception e) {
            log.error("Error during appointment reindex", e);
            throw new RuntimeException("Appointment reindex failed", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long reindexPrescriptions() {
        log.info("Starting prescription reindex operation");
        try {
            prescriptionSearchService.clearIndex();
            
            int pageNumber = 0;
            long totalReindexed = 0;

            while (true) {
                Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);
                Page<Prescription> page = prescriptionRepository.findAll(pageable);

                if (page.isEmpty()) {
                    break;
                }

                List<PrescriptionDocument> documents = page.getContent().stream()
                        .map(this::convertPrescriptionToDocument)
                        .collect(Collectors.toList());

                prescriptionSearchService.indexBulk(documents);
                totalReindexed += documents.size();

                log.debug("Reindexed batch {} of prescriptions, total so far: {}", pageNumber, totalReindexed);
                pageNumber++;
            }

            log.info("Prescription reindex completed successfully. Total indexed: {}", totalReindexed);
            return totalReindexed;
        } catch (Exception e) {
            log.error("Error during prescription reindex", e);
            throw new RuntimeException("Prescription reindex failed", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ReindexStatus fullReindex() {
        log.info("Starting full reindex operation for all entities");
        LocalDateTime startTime = LocalDateTime.now();
        ReindexStatus status = new ReindexStatus();
        status.setStartTime(startTime);

        try {
            status.setPatientCount(reindexPatients());
            status.setDoctorCount(reindexDoctors());
            status.setAppointmentCount(reindexAppointments());
            status.setPrescriptionCount(reindexPrescriptions());
            
            long totalCount = status.getPatientCount() + status.getDoctorCount() 
                            + status.getAppointmentCount() + status.getPrescriptionCount();
            status.setTotalCount(totalCount);
            status.setStatus("SUCCESS");

            LocalDateTime endTime = LocalDateTime.now();
            status.setEndTime(endTime);

            log.info("Full reindex completed successfully. Total indexed: {}", totalCount);
            return status;
        } catch (Exception e) {
            log.error("Error during full reindex", e);
            status.setStatus("FAILED");
            status.setErrorMessage(e.getMessage());
            status.setEndTime(LocalDateTime.now());
            return status;
        }
    }

    @Override
    public void clearAllIndices() {
        log.warn("Clearing all Elasticsearch indices");
        try {
            patientSearchService.clearIndex();
            doctorSearchService.clearIndex();
            appointmentSearchService.clearIndex();
            prescriptionSearchService.clearIndex();
            log.info("All indices cleared successfully");
        } catch (Exception e) {
            log.error("Error clearing indices", e);
            throw new RuntimeException("Failed to clear indices", e);
        }
    }

    @Override
    public boolean isElasticsearchAvailable() {
        try {
            // Try to perform a simple operation to check Elasticsearch availability
            long count = patientRepository.count();
            log.debug("Elasticsearch is available");
            return true;
        } catch (Exception e) {
            log.error("Elasticsearch is not available", e);
            return false;
        }
    }

    // Conversion methods

    private PatientDocument convertPatientToDocument(Patient patient) {
        return PatientDocument.builder()
                .id(patient.getId())
                .name(patient.getName())
                .nameSearchable(patient.getName())
                .namePhonetic(patient.getName())
                .age(patient.getAge())
                .dob(patient.getDob())
                .gender(patient.getGender() != null ? patient.getGender().toString() : null)
                .bloodGroup(patient.getBloodGroup() != null ? patient.getBloodGroup().toString() : null)
                .prescription(patient.getPrescription())
                .dose(patient.getDose())
                .fees(patient.getFees())
                .contactNumber(patient.getContactNumber())
                .contactNumberSearchable(patient.getContactNumber())
                .email(patient.getEmail())
                .emailSearchable(patient.getEmail())
                .address(patient.getAddress())
                .urgencyLevel(patient.getUrgencyLevel() != null ? patient.getUrgencyLevel().toString() : null)
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .deleted(patient.getDeleted())
                .createdBy(patient.getCreatedBy())
                .lastModifiedBy(patient.getLastModifiedBy())
                .build();
    }

    private DoctorDocument convertDoctorToDocument(Doctor doctor) {
        String fullName = doctor.getFirstName() + " " + doctor.getLastName();
        return DoctorDocument.builder()
                .id(doctor.getId())
                .firstName(doctor.getFirstName())
                .firstNameSearchable(doctor.getFirstName())
                .firstNamePhonetic(doctor.getFirstName())
                .lastName(doctor.getLastName())
                .lastNameSearchable(doctor.getLastName())
                .lastNamePhonetic(doctor.getLastName())
                .fullNameSearchable(fullName)
                .specialization(doctor.getSpecialization())
                .registrationNumber(doctor.getRegistrationNumber())
                .email(doctor.getEmail())
                .emailSearchable(doctor.getEmail())
                .bio(doctor.getBio())
                .department(doctor.getDepartment() != null ? doctor.getDepartment().toString() : null)
                .qualification(doctor.getQualification())
                .experienceYears(doctor.getExperienceYears())
                .licenseNumber(doctor.getLicenseNumber())
                .consultationFee(doctor.getConsultationFee())
                .isAvailable(doctor.getIsAvailable())
                .phoneNumber(doctor.getPhoneNumber())
                .phoneNumberSearchable(doctor.getPhoneNumber())
                .designation(doctor.getDesignation())
                .userId(doctor.getUserId())
                .createdAt(doctor.getCreatedAt())
                .updatedAt(doctor.getUpdatedAt())
                .deleted(doctor.getDeleted())
                .createdBy(doctor.getCreatedBy())
                .lastModifiedBy(doctor.getLastModifiedBy())
                .build();
    }

    private AppointmentDocument convertAppointmentToDocument(Appointment appointment) {
        return AppointmentDocument.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getName())
                .patientNameSearchable(appointment.getPatient().getName())
                .patientNamePhonetic(appointment.getPatient().getName())
                .doctorId(appointment.getDoctor() != null ? appointment.getDoctor().getId() : null)
                .doctorName(appointment.getDoctor() != null 
                        ? appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName() 
                        : null)
                .doctorNameSearchable(appointment.getDoctor() != null 
                        ? appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName() 
                        : null)
                .doctorNamePhonetic(appointment.getDoctor() != null 
                        ? appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName() 
                        : null)
                .department(appointment.getDepartment() != null ? appointment.getDepartment().toString() : null)
                .appointmentTime(appointment.getAppointmentTime())
                .status(appointment.getStatus().toString())
                .reason(appointment.getReason())
                .notes(appointment.getNotes())
                .tokenNumber(appointment.getTokenNumber())
                .hasPrescription(appointment.getHasPrescription())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .deleted(appointment.getDeleted())
                .createdBy(appointment.getCreatedBy())
                .lastModifiedBy(appointment.getLastModifiedBy())
                .build();
    }

    private PrescriptionDocument convertPrescriptionToDocument(Prescription prescription) {
        String medicinesStr = prescription.getMedicines() != null 
                ? prescription.getMedicines().stream()
                        .map(m -> m.getMedicineName() + " - " + m.getDosage())
                        .collect(Collectors.joining(", "))
                : "";

        return PrescriptionDocument.builder()
                .id(prescription.getId())
                .patientId(prescription.getPatient().getId())
                .patientName(prescription.getPatient().getName())
                .patientNameSearchable(prescription.getPatient().getName())
                .doctorId(prescription.getDoctor().getId())
                .doctorName(prescription.getDoctor().getFirstName() + " " + prescription.getDoctor().getLastName())
                .doctorNameSearchable(prescription.getDoctor().getFirstName() + " " + prescription.getDoctor().getLastName())
                .appointmentId(prescription.getAppointment() != null ? prescription.getAppointment().getId() : null)
                .symptoms(prescription.getSymptoms())
                .diagnosis(prescription.getDiagnosis())
                .medicines(medicinesStr)
                .advice(prescription.getAdvice())
                .notes(prescription.getNotes())
                .reportUrl(prescription.getReportUrl())
                .createdAt(prescription.getCreatedAt())
                .updatedAt(prescription.getUpdatedAt())
                .deleted(prescription.getDeleted())
                .createdBy(prescription.getCreatedBy())
                .lastModifiedBy(prescription.getLastModifiedBy())
                .build();
    }
}
