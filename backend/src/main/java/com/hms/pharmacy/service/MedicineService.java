package com.hms.pharmacy.service;

import com.hms.pharmacy.dto.request.DispenseMedicineRequestDTO;
import com.hms.pharmacy.dto.request.MedicineRequestDTO;
import com.hms.pharmacy.dto.response.InventoryTransactionResponseDTO;
import com.hms.pharmacy.dto.response.MedicineResponseDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface MedicineService {

    MedicineResponseDTO createMedicine(MedicineRequestDTO dto);

    Page<MedicineResponseDTO> searchMedicines(
            String query,
            String category,
            Boolean isActive,
            Pageable pageable
    );

    MedicineResponseDTO updateMedicine(Long id, MedicineRequestDTO dto);

    void deleteMedicine(Long id);

    MedicineResponseDTO getMedicineById(Long id);

    List<MedicineResponseDTO> getAllMedicines();

    List<MedicineResponseDTO> getActiveMedicines();

    List<MedicineResponseDTO> getMedicinesByCategory(String category);

    boolean existsByMedicineCode(String medicineCode);

    void dispenseMedicines(DispenseMedicineRequestDTO request);

    void restockMedicine(Long id, Integer quantity);

    List<InventoryTransactionResponseDTO> getAllTransactions();
}
