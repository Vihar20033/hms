package com.hms.pharmacy.repository;

import com.hms.pharmacy.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    // SELECT * FROM inventory_transactions WHERE medicine_id = :medicineId AND deleted = false
    List<InventoryTransaction> findByMedicineId(Long medicineId);

    // SELECT * FROM inventory_transactions WHERE reference_id = :referenceId AND deleted = false
    List<InventoryTransaction> findByReferenceId(Long referenceId);
}
