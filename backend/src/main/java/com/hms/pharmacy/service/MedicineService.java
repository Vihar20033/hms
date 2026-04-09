package com.hms.pharmacy.service;

import com.hms.pharmacy.dto.request.DispenseMedicineRequestDTO;
import com.hms.pharmacy.dto.request.MedicineRequestDTO;
import com.hms.pharmacy.dto.response.InventoryTransactionResponseDTO;
import com.hms.pharmacy.dto.response.MedicineResponseDTO;
import org.springframework.data.domain.Slice;

import java.util.List;


public interface MedicineService {

    MedicineResponseDTO createMedicine(MedicineRequestDTO dto);

    MedicineResponseDTO updateMedicine(Long id, MedicineRequestDTO dto);

    void deleteMedicine(Long id);

    MedicineResponseDTO getMedicineById(Long id);

    List<MedicineResponseDTO> getAllMedicines();

    Slice<MedicineResponseDTO> getMedicineSlice(int page, int size);

    Slice<MedicineResponseDTO> getSearchableMedicineSlice(int page, int size, String query);

    List<MedicineResponseDTO> getActiveMedicines();

    List<MedicineResponseDTO> getMedicinesByCategory(String category);

    boolean existsByMedicineCode(String medicineCode);

    void dispenseMedicines(DispenseMedicineRequestDTO request);

    /** Fix #5 – Long quantity to prevent overflow on large restock transactions */
    void restockMedicine(Long id, Long quantity);

    List<InventoryTransactionResponseDTO> getAllTransactions();

    Slice<InventoryTransactionResponseDTO> getTransactionSlice(int page, int size);
}
