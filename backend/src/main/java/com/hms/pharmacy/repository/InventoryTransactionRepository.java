package com.hms.pharmacy.repository;

import com.hms.pharmacy.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, UUID> {
    // SELECT * FROM inventory_transactions WHERE medicine_id = :medicineId AND deleted = false
    List<InventoryTransaction> findByMedicineId(UUID medicineId);

    // SELECT * FROM inventory_transactions WHERE reference_id = :referenceId AND deleted = false
    List<InventoryTransaction> findByReferenceId(UUID referenceId);
}
