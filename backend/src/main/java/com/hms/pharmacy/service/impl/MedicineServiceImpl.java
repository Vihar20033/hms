package com.hms.pharmacy.service.impl;

import com.hms.pharmacy.dto.request.MedicineRequestDTO;
import com.hms.pharmacy.dto.response.MedicineResponseDTO;
import com.hms.pharmacy.entity.Medicine;
import com.hms.pharmacy.exception.DuplicateMedicineException;
import com.hms.pharmacy.exception.MedicineNotFoundException;
import com.hms.pharmacy.dto.request.DispenseMedicineRequestDTO;
import com.hms.pharmacy.entity.InventoryTransaction;
import com.hms.pharmacy.repository.InventoryTransactionRepository;
import com.hms.pharmacy.mapper.MedicineMapper;
import com.hms.pharmacy.repository.MedicineRepository;
import com.hms.pharmacy.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;
    private final MedicineMapper medicineMapper;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final com.hms.common.audit.AuditLogService auditLogService;

    @Override
    @Transactional
    public MedicineResponseDTO createMedicine(MedicineRequestDTO dto) {
        if (medicineRepository.existsByMedicineCode(dto.getMedicineCode())) {
            throw new DuplicateMedicineException("Medicine code already exists: " + dto.getMedicineCode());
        }

        Medicine medicine = medicineMapper.toEntity(dto);
        Medicine savedMedicine = medicineRepository.save(medicine);

        // Record initial inventory transaction
        InventoryTransaction transaction = InventoryTransaction.builder()
                .medicine(savedMedicine)
                .transactionType("IN")
                .quantity(savedMedicine.getQuantityInStock())
                .referenceId(savedMedicine.getId())
                .notes("Initial stock at creation")
                .build();
        inventoryTransactionRepository.save(transaction);

        auditLogService.log(getCurrentUsername(), "MEDICINE_CREATE", "Medicine", savedMedicine.getId().toString(), "name=" + savedMedicine.getName());
        return medicineMapper.toDto(savedMedicine);
    }

    @Override
    @Transactional
    public MedicineResponseDTO updateMedicine(UUID id, MedicineRequestDTO dto) {
        Medicine existingMedicine = medicineRepository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with ID: " + id));

        if (!existingMedicine.getMedicineCode().equals(dto.getMedicineCode())
                && medicineRepository.existsByMedicineCode(dto.getMedicineCode())) {
            throw new DuplicateMedicineException("Medicine code already exists: " + dto.getMedicineCode());
        }

        medicineMapper.updateEntityFromDto(dto, existingMedicine);
        Medicine updatedMedicine = medicineRepository.save(existingMedicine);
        auditLogService.log(getCurrentUsername(), "MEDICINE_UPDATE", "Medicine", id.toString(), "name=" + updatedMedicine.getName());
        return medicineMapper.toDto(updatedMedicine);
    }

    @Override
    @Transactional
    public void deleteMedicine(UUID id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with ID: " + id));

        medicine.setIsActive(false);
        medicine.setDeleted(true);
        medicineRepository.save(medicine);
        auditLogService.log(getCurrentUsername(), "MEDICINE_DELETE", "Medicine", id.toString(), "name=" + medicine.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public MedicineResponseDTO getMedicineById(UUID id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with ID: " + id));

        return medicineMapper.toDto(medicine);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineResponseDTO> getAllMedicines() {
        return medicineMapper.toDtoList(medicineRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineResponseDTO> getActiveMedicines() {
        return medicineMapper.toDtoList(medicineRepository.findByIsActiveTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineResponseDTO> getMedicinesByCategory(String category) {
        return medicineMapper.toDtoList(medicineRepository.findByCategory(category));
    }


    @Override
    @Transactional(readOnly = true)
    public boolean existsByMedicineCode(String medicineCode) {
        return medicineRepository.existsByMedicineCode(medicineCode);
    }

    @Override
    @Transactional
    public void dispenseMedicines(DispenseMedicineRequestDTO request) {
        for (DispenseMedicineRequestDTO.DispenseItemDTO item : request.getItems()) {
            Medicine medicine = medicineRepository.findById(item.getMedicineId())
                    .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with ID: " + item.getMedicineId()));

            int updatedRows = medicineRepository.deductStockAtomic(medicine.getId(), item.getQuantity());
            if (updatedRows == 0) {
                throw new RuntimeException("Insufficient stock for medicine: " + medicine.getName());
            }

            InventoryTransaction transaction = InventoryTransaction.builder()
                    .medicine(medicine)
                    .transactionType("OUT")
                    .quantity(item.getQuantity())
                    .referenceId(request.getPrescriptionId())
                    .notes("Dispensed against prescription")
                    .build();

            inventoryTransactionRepository.save(transaction);
            auditLogService.log(getCurrentUsername(), "MEDICINE_DISPENSE", "Medicine", item.getMedicineId().toString(), "qty=" + item.getQuantity());
        }
    }



    @Override
    @Transactional(readOnly = true)
    public List<com.hms.pharmacy.dto.response.InventoryTransactionResponseDTO> getAllTransactions() {
        return inventoryTransactionRepository.findAll().stream()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt())) // Recent first
                .map(t -> com.hms.pharmacy.dto.response.InventoryTransactionResponseDTO.builder()
                        .id(t.getId())
                        .medicineId(t.getMedicine().getId())
                        .medicineName(t.getMedicine().getName())
                        .medicineCode(t.getMedicine().getMedicineCode())
                        .transactionType(t.getTransactionType())
                        .quantity(t.getQuantity())
                        .referenceId(t.getReferenceId())
                        .notes(t.getNotes())
                        .createdAt(t.getCreatedAt())
                        .createdBy(t.getCreatedBy())
                        .build())
                .toList();
    }

    private String getCurrentUsername() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }
}
