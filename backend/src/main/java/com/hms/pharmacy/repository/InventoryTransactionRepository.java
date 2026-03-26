package com.hms.pharmacy.repository;

import com.hms.pharmacy.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    List<InventoryTransaction> findByMedicineId(Long medicineId);

    List<InventoryTransaction> findByReferenceId(Long referenceId);
}
