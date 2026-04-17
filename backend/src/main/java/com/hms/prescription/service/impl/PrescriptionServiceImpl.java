package com.hms.prescription.service.impl;

import com.hms.appointment.entity.Appointment;
import com.hms.appointment.exception.AppointmentNotFoundException;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.common.audit.AuditLogService;
import com.hms.common.enums.Role;
import com.hms.doctor.entity.Doctor;
import com.hms.doctor.exception.DoctorNotFoundException;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.patient.entity.Patient;
import com.hms.patient.exception.PatientNotFoundException;
import com.hms.patient.repository.PatientRepository;
import com.hms.pharmacy.entity.InventoryTransaction;
import com.hms.pharmacy.entity.Medicine;
import com.hms.pharmacy.exception.InsufficientStockException;
import com.hms.pharmacy.exception.MedicineNotFoundException;
import com.hms.pharmacy.repository.InventoryTransactionRepository;
import com.hms.pharmacy.repository.MedicineRepository;
import com.hms.prescription.dto.request.PrescriptionMedicineRequestDTO;
import com.hms.prescription.dto.request.PrescriptionRequestDTO;
import com.hms.prescription.dto.response.PrescriptionResponseDTO;
import com.hms.prescription.entity.Prescription;
import com.hms.prescription.entity.PrescriptionMedicine;
import com.hms.prescription.exception.PrescriptionNotFoundException;
import com.hms.prescription.mapper.PrescriptionMapper;
import com.hms.prescription.repository.PrescriptionRepository;
import com.hms.prescription.service.PrescriptionService;
import com.hms.common.service.PdfGenerationService;
import com.hms.common.service.CloudinaryService;
import com.hms.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hms.common.exception.BadRequestException;
import com.hms.common.concurrency.LockService;
import java.util.concurrent.TimeUnit;

import java.util.Collections;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionMapper prescriptionMapper;
    private final MedicineRepository medicineRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final AuditLogService auditLogService;
    private final PdfGenerationService pdfGenerationService;
    private final CloudinaryService cloudinaryService;
    private final LockService lockService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrescriptionResponseDTO createPrescription(PrescriptionRequestDTO dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException("Patient not found: " + dto.getPatientId()));

        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found: " + dto.getDoctorId()));

        Prescription prescription = prescriptionMapper.toEntity(dto);
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);

        if (dto.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                    .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + dto.getAppointmentId()));
            prescription.setAppointment(appointment);
        }

        // Fix #7 - Concurrent Patient Soft-Delete
        // Check if patient was deleted between retrieval and start of prescription creation
        if (patient.isDeleted()) {
            throw new BadRequestException("Cannot create a prescription for a deleted patient profile.");
        }

        if (dto.getMedicines() != null && !dto.getMedicines().isEmpty()) {
            prescription.getMedicines().clear();
            for (PrescriptionMedicineRequestDTO medicineDto : dto.getMedicines()) {
                PrescriptionMedicine medicine = prescriptionMapper.toMedicineEntity(medicineDto);
                prescription.addMedicine(medicine);
            }
        }

        Prescription savedPrescription = prescriptionRepository.save(prescription);

        if (dto.getMedicines() != null && !dto.getMedicines().isEmpty()) {
            for (PrescriptionMedicineRequestDTO medicineDto : dto.getMedicines()) {
                String lockKey = "lock:medicine:" + medicineDto.getMedicineName().toLowerCase().replace(" ", "_");
                
                if (lockService.tryLock(lockKey, 10, 30, TimeUnit.SECONDS)) {
                    try {
                        Medicine med = medicineRepository.findByNameIgnoreCase(medicineDto.getMedicineName())
                                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found: " + medicineDto.getMedicineName()));
                        
                        Long qty = medicineDto.getQuantity() != null ? medicineDto.getQuantity() : 1L;
                        
                        // Fix #8 - Prescribed Dosage Range Validation
                        if (med.getMaxSafeDose() != null && qty > med.getMaxSafeDose()) {
                            log.warn("SAFETY ALERT: Prescription for {} exceeds max safe dose ({} > {}). Logging for review.", 
                                    med.getName(), qty, med.getMaxSafeDose());
                        }

                        // Snapshotting: Store name and price at this exact moment
                        PrescriptionMedicine pm = prescriptionMapper.toMedicineEntity(medicineDto);
                        pm.setMedicineName(med.getName()); // Use master name
                        pm.setUnitPriceAtPrescription(med.getUnitPrice());
                        pm.setPrescription(savedPrescription);
                        savedPrescription.getMedicines().add(pm);

                        int updatedRows = medicineRepository.deductStockAtomic(med.getId(), qty);
                        if (updatedRows == 0) {
                            throw new InsufficientStockException(med.getName(), "Insufficient stock for: " + med.getName());
                        }

                        // ... rest of logging and transaction ...
                        if (med.getQuantityInStock() - qty <= med.getReorderLevel()) {
                            auditLogService.log(null, "LOW_STOCK_AUTO_ALERT", "Medicine", med.getId().toString(), 
                                "Medicine " + med.getName() + " running low.");
                        }
                        
                        InventoryTransaction transaction = InventoryTransaction.builder()
                                .medicine(med)
                                .transactionType("DISPENSE")
                                .quantity(qty)
                                .referenceId(savedPrescription.getId())
                                .notes("Dispensed")
                                .build();
                        inventoryTransactionRepository.save(transaction);
                    } finally {
                        lockService.unlock(lockKey);
                    }
                } else {
                    throw new BadRequestException("Could not acquire lock for medicine: " + medicineDto.getMedicineName());
                }
            }
            // Save again to persist the snapshotting additions
            savedPrescription = prescriptionRepository.save(savedPrescription);
        }

        // Generate and Upload PDF
        try {
            byte[] pdfBytes = pdfGenerationService.generatePrescriptionPdf(savedPrescription);
            String reportUrl = cloudinaryService.uploadBytes(pdfBytes, "rx_" + savedPrescription.getId(), "prescriptions");
            savedPrescription.setReportUrl(reportUrl);
            savedPrescription = prescriptionRepository.save(savedPrescription);
        } catch (Exception e) {
            log.error("Failed to generate/upload prescription PDF", e);
        }

        return prescriptionMapper.toDto(savedPrescription);
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponseDTO getPrescriptionById(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new PrescriptionNotFoundException("Prescription not found: " + id, id.toString()));
        
        checkOwnership(prescription);
        return prescriptionMapper.toDto(prescription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDTO> getAllPrescriptions() {
        // Fix #10 - Empty Search Performance 
        // This method is deprecated. Callers should use paginated getPrescriptionSlice instead.
        log.warn("Unbounded getAllPrescriptions called. Enforcing limit to prevent system crash.");
        PageRequest request = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdAt"));
        return prescriptionRepository.findAll(request).getContent().stream()
                .map(prescriptionMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<PrescriptionResponseDTO> getPrescriptionSlice(int page, int size) {
        return getPrescriptionSlice(page, size, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<PrescriptionResponseDTO> getPrescriptionSlice(int page, int size, String query) {
        PageRequest request = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Role role = user.getRole();
        String normalizedQuery = query == null ? "" : query.trim();

        if (role == Role.ADMIN || role == Role.PHARMACIST) {
            if (!normalizedQuery.isBlank()) {
                return prescriptionRepository.searchPrescriptions(normalizedQuery, request).map(prescriptionMapper::toDto);
            }
            return prescriptionRepository.findAll(request).map(prescriptionMapper::toDto);
        }

        if (role == Role.DOCTOR) {
            if (!normalizedQuery.isBlank()) {
                return prescriptionRepository.searchPrescriptionsForDoctor(
                        user.getId(),
                        normalizedQuery,
                        request).map(prescriptionMapper::toDto);
            }
            return prescriptionRepository.findByDoctorUserId(user.getId(), request).map(prescriptionMapper::toDto);
        }

        return new SliceImpl<>(Collections.emptyList(), request, false);
    }


    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDTO> getPrescriptionsByPatientId(Long patientId) {
        // Fix #10 - Empty Search Performance: Enforce limit
        return getPrescriptionSliceByPatient(patientId, 0, 50).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<PrescriptionResponseDTO> getPrescriptionSliceByPatient(Long patientId, int page, int size) {
        PageRequest request = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Slice<Prescription> slice = prescriptionRepository.findByPatientId(patientId, request);
        if (!slice.isEmpty()) {
            checkOwnership(slice.getContent().get(0));
        }
        return slice.map(prescriptionMapper::toDto);
    }

    @Override
    @Transactional
    public void deletePrescription(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new PrescriptionNotFoundException("Prescription not found: " + id, id.toString()));
        
        if (prescription.getMedicines() != null && !prescription.getMedicines().isEmpty()) {
            for (PrescriptionMedicine pm : prescription.getMedicines()) {
                medicineRepository.findByNameIgnoreCase(pm.getMedicineName()).ifPresent(med -> {
                    Long qty = pm.getQuantity() != null ? pm.getQuantity() : 1L;
                    medicineRepository.addStockAtomic(med.getId(), qty);
                    
                    InventoryTransaction transaction = InventoryTransaction.builder()
                            .medicine(med)
                            .transactionType("IN")
                            .quantity(qty)
                            .referenceId(prescription.getId())
                            .notes("Cancelled")
                            .build();
                    inventoryTransactionRepository.save(transaction);
                });
            }
        }

        // Fix #6 - PDF Report Orphaned Files
        if (prescription.getReportUrl() != null) {
            try {
                // Extract public ID from Cloudinary URL (e.g. .../v12345/hms/prescriptions/rx_123.pdf)
                String url = prescription.getReportUrl();
                String folderPath = "hms/prescriptions/";
                int startIndex = url.indexOf(folderPath);
                if (startIndex != -1) {
                    int endIndex = url.lastIndexOf(".");
                    String publicId = url.substring(startIndex, endIndex);
                    cloudinaryService.deleteFile(publicId);
                }
            } catch (Exception e) {
                log.error("Failed to delete Cloudinary file while deleting prescription {}", id, e);
            }
        }

        prescriptionRepository.delete(prescription);
        auditLogService.log(com.hms.security.util.SecurityUtils.getCurrentUsername(), "PRESCRIPTION_DELETE", "Prescription", id.toString(), "deleted=true");
    }

    private void checkOwnership(Prescription prescription) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Role role = user.getRole();

        // 1. Staff with Access
        if (role == Role.ADMIN || role == Role.PHARMACIST) {
            return;
        }

        // 2. Doctor access (Assigned to the patient or wrote it)
        if (role == Role.DOCTOR) {
            if (prescription.getDoctor().getUserId().equals(user.getId())) {
                return;
            }
        }

        log.warn("Security Alert: User {} with role {} tried to access prescription {}.", 
                user.getUsername(), role, prescription.getId());
        throw new AccessDeniedException("You do not have permission to access this prescription.");
    }
}
