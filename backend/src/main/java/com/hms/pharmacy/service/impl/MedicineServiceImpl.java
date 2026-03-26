package com.hms.pharmacy.service.impl;

import com.hms.common.audit.AuditLogService;
import com.hms.common.enums.MedicineCategory;
import com.hms.pharmacy.dto.request.MedicineRequestDTO;
import com.hms.pharmacy.dto.response.InventoryTransactionResponseDTO;
import com.hms.pharmacy.dto.response.MedicineResponseDTO;
import com.hms.pharmacy.entity.Medicine;
import com.hms.pharmacy.exception.DuplicateMedicineException;
import com.hms.pharmacy.exception.InsufficientStockException;
import com.hms.pharmacy.exception.MedicineNotFoundException;
import com.hms.pharmacy.dto.request.DispenseMedicineRequestDTO;
import com.hms.pharmacy.entity.InventoryTransaction;
import com.hms.pharmacy.repository.InventoryTransactionRepository;
import com.hms.pharmacy.mapper.MedicineMapper;
import com.hms.pharmacy.repository.MedicineRepository;
import com.hms.pharmacy.service.MedicineService;
import com.hms.pharmacy.specification.MedicineSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;
    private final MedicineMapper medicineMapper;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final AuditLogService auditLogService;

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
    @Transactional(readOnly = true)
    public Page<MedicineResponseDTO> searchMedicines(String query, String category, Boolean isActive, Pageable pageable) {
        MedicineCategory cat = (category != null && !category.isBlank()) ? MedicineCategory.valueOf(category) : null;
        
        Specification<Medicine> spec = Specification.where(MedicineSpecification.fuzzySearch(query))
                .and(MedicineSpecification.fuzzySearch(query))
                .and(MedicineSpecification.hasCategory(cat))
                .and(MedicineSpecification.isActive(isActive));

        return medicineRepository.findAll(spec, pageable).map(medicineMapper::toDto);
    }

    @Override
    @Transactional
    public MedicineResponseDTO updateMedicine(Long id, MedicineRequestDTO dto) {
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
    public void deleteMedicine(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with ID: " + id));

        medicineRepository.delete(medicine);
        auditLogService.log(getCurrentUsername(), "MEDICINE_DELETE", "Medicine", id.toString(), "name=" + medicine.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public MedicineResponseDTO getMedicineById(Long id) {
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
        return medicineMapper.toDtoList(medicineRepository.findByCategory(MedicineCategory.valueOf(category)));
    }


    @Override
    @Transactional(readOnly = true)
    public boolean existsByMedicineCode(String medicineCode) {
        return medicineRepository.existsByMedicineCode(medicineCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dispenseMedicines(DispenseMedicineRequestDTO request) {
        for (DispenseMedicineRequestDTO.DispenseItemDTO item : request.getItems()) {
            Medicine medicine = medicineRepository.findById(item.getMedicineId())
                    .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with ID: " + item.getMedicineId()));

            int updatedRows = medicineRepository.deductStockAtomic(medicine.getId(), item.getQuantity());
            if (updatedRows == 0) {
                throw new InsufficientStockException(medicine.getName(), "Insufficient stock for medicine: " + medicine.getName());
            }

            InventoryTransaction transaction = InventoryTransaction.builder()
                    .medicine(medicine)
                    .transactionType("OUT")
                    .quantity(item.getQuantity())
                    .referenceId(request.getPrescriptionId())
                    .notes("Dispensed against manual request")
                    .build();

            inventoryTransactionRepository.save(transaction);
            auditLogService.log(getCurrentUsername(), "MEDICINE_DISPENSE", "Medicine", item.getMedicineId().toString(), "qty=" + item.getQuantity());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restockMedicine(Long id, Integer quantity) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException("Medicine not found with ID: " + id));

        medicineRepository.addStockAtomic(medicine.getId(), quantity);

        InventoryTransaction transaction = InventoryTransaction.builder()
                .medicine(medicine)
                .transactionType("IN")
                .quantity(quantity)
                .referenceId(id)
                .notes("Manual Restock")
                .build();

        inventoryTransactionRepository.save(transaction);
        auditLogService.log(getCurrentUsername(), "MEDICINE_RESTOCK", "Medicine", id.toString(), "qty=" + quantity);
    }



    @Override
    @Transactional(readOnly = true)
    public List<InventoryTransactionResponseDTO> getAllTransactions() {
        return inventoryTransactionRepository.findAll().stream()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt())) // Recent first
                .map(t -> InventoryTransactionResponseDTO.builder()
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }
}
