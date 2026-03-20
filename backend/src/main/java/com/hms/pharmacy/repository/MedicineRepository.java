package com.hms.pharmacy.repository;

import com.hms.pharmacy.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, UUID>, JpaSpecificationExecutor<Medicine> {

    Optional<Medicine> findByMedicineCode(String medicineCode);

    boolean existsByMedicineCode(String medicineCode);

    List<Medicine> findByIsActiveTrue();

    List<Medicine> findByCategory(String category);

    List<Medicine> findByQuantityInStockLessThanEqual(Integer reorderLevel);

    Optional<Medicine> findByNameIgnoreCase(String name);

    @Modifying
    @Query("UPDATE Medicine m SET m.quantityInStock = m.quantityInStock - :qty " +
           "WHERE m.id = :id AND m.quantityInStock >= :qty")
    int deductStockAtomic(@Param("id") UUID id, @Param("qty") Integer qty);

    @Modifying
    @Query("UPDATE Medicine m SET m.quantityInStock = m.quantityInStock + :qty " +
           "WHERE m.id = :id")
    void addStockAtomic(@Param("id") UUID id, @Param("qty") Integer qty);
}
