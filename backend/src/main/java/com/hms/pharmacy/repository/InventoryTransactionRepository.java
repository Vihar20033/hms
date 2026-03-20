package com.hms.pharmacy.repository;

import com.hms.pharmacy.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, UUID> {
    List<InventoryTransaction> findByMedicineId(UUID medicineId);
    List<InventoryTransaction> findByReferenceId(UUID referenceId);
}
