package com.hms.pharmacy.service;

import com.hms.pharmacy.dto.request.DispenseMedicineRequestDTO;
import com.hms.pharmacy.dto.request.MedicineRequestDTO;
import com.hms.pharmacy.dto.response.InventoryTransactionResponseDTO;
import com.hms.pharmacy.dto.response.MedicineResponseDTO;

import java.util.List;
import java.util.UUID;

public interface MedicineService {

    MedicineResponseDTO createMedicine(MedicineRequestDTO dto);

    MedicineResponseDTO updateMedicine(UUID id, MedicineRequestDTO dto);

    void deleteMedicine(UUID id);

    MedicineResponseDTO getMedicineById(UUID id);

    List<MedicineResponseDTO> getAllMedicines();

    List<MedicineResponseDTO> getActiveMedicines();

    List<MedicineResponseDTO> getMedicinesByCategory(String category);

    boolean existsByMedicineCode(String medicineCode);

    void dispenseMedicines(DispenseMedicineRequestDTO request);

    void restockMedicine(UUID id, Integer quantity);

    List<InventoryTransactionResponseDTO> getAllTransactions();
}
