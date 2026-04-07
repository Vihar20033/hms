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

    List<MedicineResponseDTO> getActiveMedicines();

    List<MedicineResponseDTO> getMedicinesByCategory(String category);

    boolean existsByMedicineCode(String medicineCode);

    void dispenseMedicines(DispenseMedicineRequestDTO request);

    void restockMedicine(Long id, Integer quantity);

    List<InventoryTransactionResponseDTO> getAllTransactions();
}
