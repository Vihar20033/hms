package com.hms.prescription.service.impl;

import com.hms.appointment.entity.Appointment;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.doctor.entity.Doctor;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.patient.entity.Patient;
import com.hms.patient.repository.PatientRepository;
import com.hms.pharmacy.exception.InsufficientStockException;
import com.hms.prescription.dto.request.PrescriptionRequestDTO;
import com.hms.prescription.dto.response.PrescriptionResponseDTO;
import com.hms.prescription.entity.Prescription;
import com.hms.prescription.entity.PrescriptionMedicine;
import com.hms.prescription.mapper.PrescriptionMapper;
import com.hms.prescription.repository.PrescriptionRepository;
import com.hms.prescription.service.PrescriptionService;
import com.hms.pharmacy.repository.MedicineRepository;
import com.hms.pharmacy.entity.InventoryTransaction;
import com.hms.pharmacy.repository.InventoryTransactionRepository;
import com.hms.common.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionMapper prescriptionMapper;
    private final MedicineRepository medicineRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public PrescriptionResponseDTO createPrescription(PrescriptionRequestDTO dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Prescription prescription = prescriptionMapper.toEntity(dto);
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);

        if (dto.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            prescription.setAppointment(appointment);
        }

        if (dto.getMedicines() != null && !dto.getMedicines().isEmpty()) {
            prescription.getMedicines().clear();
            for (PrescriptionRequestDTO.PrescriptionMedicineRequestDTO medicineDto : dto.getMedicines()) {
                PrescriptionMedicine medicine = prescriptionMapper.toMedicineEntity(medicineDto);
                prescription.addMedicine(medicine);
            }
        }

        Prescription savedPrescription = prescriptionRepository.save(prescription);

        if (dto.getMedicines() != null && !dto.getMedicines().isEmpty()) {
            for (PrescriptionRequestDTO.PrescriptionMedicineRequestDTO medicineDto : dto.getMedicines()) {
                com.hms.pharmacy.entity.Medicine med = medicineRepository.findByNameIgnoreCase(medicineDto.getMedicineName())
                        .orElseThrow(() -> new IllegalArgumentException("Medicine not found: " + medicineDto.getMedicineName()));
                
                Integer qty = medicineDto.getQuantity() != null ? medicineDto.getQuantity() : 1;
                
                int updatedRows = medicineRepository.deductStockAtomic(med.getId(), qty);
                if (updatedRows == 0) {
                    throw new InsufficientStockException(med.getName(), "Insufficient stock for medicine: " + med.getName() + " (Requested: " + qty + ", Available: " + med.getQuantityInStock() + ")");
                }

                // Automatic Low Stock Detection
                if (med.getQuantityInStock() - qty <= med.getReorderLevel()) {
                    auditLogService.log(null, "LOW_STOCK_AUTO_ALERT", "Medicine", med.getId().toString(), 
                        "Medicine " + med.getName() + " is running low after prescription. Current stock: " + (med.getQuantityInStock() - qty));
                }
                
                InventoryTransaction transaction = InventoryTransaction.builder()
                        .medicine(med)
                        .transactionType("OUT")
                        .quantity(qty)
                        .referenceId(savedPrescription.getId())
                        .notes("Dispensed against prescription")
                        .build();
                inventoryTransactionRepository.save(transaction);
            }
        }

        return prescriptionMapper.toDto(savedPrescription);
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponseDTO getPrescriptionById(UUID id) {
        return prescriptionRepository.findById(id)
                .map(prescriptionMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDTO> getAllPrescriptions() {
        return prescriptionMapper.toDtoList(prescriptionRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDTO> getPrescriptionsByPatientId(UUID patientId) {
        return prescriptionMapper.toDtoList(prescriptionRepository.findByPatientId(patientId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDTO> getPrescriptionsByDoctorId(UUID doctorId) {
        return prescriptionMapper.toDtoList(prescriptionRepository.findByDoctorId(doctorId));
    }

    @Override
    @Transactional
    public void deletePrescription(UUID id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));
        
        if (prescription.getMedicines() != null && !prescription.getMedicines().isEmpty()) {
            for (PrescriptionMedicine pm : prescription.getMedicines()) {
                medicineRepository.findByNameIgnoreCase(pm.getMedicineName()).ifPresent(med -> {
                    Integer qtyToRefund = pm.getQuantity() != null ? pm.getQuantity() : 1;
                    
                    medicineRepository.addStockAtomic(med.getId(), qtyToRefund);
                    
                    InventoryTransaction transaction = InventoryTransaction.builder()
                            .medicine(med)
                            .transactionType("IN")
                            .quantity(qtyToRefund)
                            .referenceId(prescription.getId())
                            .notes("Stock refunded due to cancelled/deleted prescription")
                            .build();
                    inventoryTransactionRepository.save(transaction);
                });
            }
        }

        prescription.setDeleted(true);
        prescriptionRepository.save(prescription);
    }
}
